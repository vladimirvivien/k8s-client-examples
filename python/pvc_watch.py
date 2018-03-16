import os
import pint
from kubernetes import client, config, watch


def main():
    # setup the namespace
    ns = os.getenv("K8S_NAMESPACE")
    if ns is None:
        ns = ""

    # use package pint to handle volume quantities
    unit = pint.UnitRegistry()
    unit.define('gibi = 2**30 = Gi')
    max_claims = unit.Quantity("150Gi")
    total_claims = unit.Quantity("0Gi")

    # configure client 
    config.load_kube_config()
    api = client.CoreV1Api()

    # Print PVC list
    pvcs = api.list_namespaced_persistent_volume_claim(namespace=ns, watch=False)
    print("")
    print("---- PVCs ---")
    print("%-16s\t%-40s\t%-6s" % ("Name", "Volume", "Size"))
    for pvc in pvcs.items:
        print("%-16s\t%-40s\t%-6s" %
              (pvc.metadata.name, pvc.spec.volume_name, pvc.spec.resources.requests['storage']))
    print("")


    # setup watch
    w = watch.Watch()
    for item in w.stream(api.list_namespaced_persistent_volume_claim, namespace=ns, timeout_seconds=0):
        pvc = item['object']
        

        # parse PVC events
        # new PVC added
        if item['type'] == 'ADDED':
            size = pvc.spec.resources.requests['storage']
            claimQty = unit.Quantity(size)
            total_claims = total_claims + claimQty

            print("PVC Added: %s; size %s" % (pvc.metadata.name, size))

            if total_claims >= max_claims:
                print("---------------------------------------------")
                print("WARNING: claim overage reached; max %s; at %s" % (max_claims, total_claims))
                print("**** Trigger over capacity action ***")
                print("---------------------------------------------")
        
        # PVC is removed
        if item['type'] == 'DELETED':
            size = pvc.spec.resources.requests['storage']
            claimQty = unit.Quantity(size)
            total_claims = total_claims - claimQty

            print("PVC Deleted: %s; size %s" % (pvc.metadata.name, size))

            if total_claims <= max_claims:
                print("---------------------------------------------")
                print("INFO: claim usage normal; max %s; at %s" % (max_claims, total_claims))
                print("---------------------------------------------")

        
        # PVC is UPDATED
        if item['type'] == "MODIFIED":
            print("MODIFIED: %s" % (pvc.metadata.name))

        print("INFO: total PVC at %4.1f%% capacity" % ((total_claims/max_claims)*100))

if __name__ == '__main__':
    main()
