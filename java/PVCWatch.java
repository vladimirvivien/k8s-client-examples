import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.Pair;
import io.kubernetes.client.auth.*;
import io.kubernetes.client.util.*;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.Quantity.Format;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;

public class PVCWatch {
    public static void main(String[] args) throws IOException{
        // K8S_NAMESPACE defaults to empty
        String namespace = System.getenv("K8S_NAMESPACE");
        if (namespace == null || "".equals(namespace)) {
            namespace = "default";
        }

        Quantity maxClaims = Quantity.fromString("150Gi");
        Quantity totalClaims = Quantity.fromString("0Gi");

        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        //client.setDebugging(true);

        String apiServer = client.getBasePath();      
        System.out.format("%nconnecting to API server %s %n%n", apiServer);

        client.getHttpClient().setReadTimeout(300, TimeUnit.SECONDS);
        CoreV1Api api = new CoreV1Api(client);
        V1PersistentVolumeClaimList list = null;
        try{
            list = api.listNamespacedPersistentVolumeClaim(namespace, null, null, null, null, null, null, null, null, null);
        }catch(ApiException apie){
            System.err.println("Exception when calling CoreV1Api#listNamespacedPersistentVolumeClaim");
            apie.printStackTrace();
            System.exit(1);
        }
        PVCWatch.printPVCs(list);

        // parse watched events
        System.out.format("%n----- PVC Watch (max total claims: %s) -----", maxClaims.toSuffixedString());
        try {
            Watch<V1PersistentVolumeClaim> watch = Watch.createWatch(
                    client,
                    api.listNamespacedPersistentVolumeClaimCall(
                            namespace, null, null, null, null, null, null, null, null, Boolean.TRUE, null, null),
                    new TypeToken<Watch.Response<V1PersistentVolumeClaim>>() {
                    }.getType()
            );
            // let's watch for total PVC sizes
            for (Watch.Response<V1PersistentVolumeClaim> item : watch) {
                V1PersistentVolumeClaim pvc = item.object;
                String claimSize = null;
                Quantity claimQuant = null;
                BigDecimal totalNum = null;

                switch (item.type){
                    case "ADDED":
                        claimSize = pvc.getSpec().getResources().getRequests().get("storage");
                        claimQuant = Quantity.fromString(claimSize);
                        totalNum = totalClaims.getNumber().add(claimQuant.getNumber());
                        totalClaims = new Quantity(totalNum, Format.BINARY_SI);

                        System.out.format(
                            "%nADDED: PVC %s added, size %s", 
                            pvc.getMetadata().getName(), claimSize
                        );

                        // claim size overage ?
                        if (totalClaims.getNumber().compareTo(maxClaims.getNumber()) >= 1) {
                                System.out.format(
                                    "%nWARNING: claim overage reached: max %s, at %s", 
                                    maxClaims.toSuffixedString(), totalClaims.toSuffixedString()
                                );
                                System.out.format("%n*** Trigger over capacity action ***");
                        }
                        break;
                    case "MODIFIED":
                        System.out.format("%nMODIFIED: PVC %s", pvc.getMetadata().getName());
                        break;
                    case "DELETED":
                         claimSize = pvc.getSpec().getResources().getRequests().get("storage");
                         claimQuant = Quantity.fromString(claimSize);
                         totalNum = totalClaims.getNumber().subtract(claimQuant.getNumber());
                         totalClaims = new Quantity(totalNum, Format.BINARY_SI);

                        System.out.format(
                            "%nDELETED: PVC %s removed, size %s", 
                            pvc.getMetadata().getName(), claimSize
                        );

                        // size back to normal ?
                        if (totalClaims.getNumber().compareTo(maxClaims.getNumber()) <= 0) {
                                System.out.format(
                                    "%nINFO: claim usage normal: max %s, at %s", 
                                    maxClaims.toSuffixedString(), totalClaims.toSuffixedString()
                                );
                        }
                        break;
                }
                System.out.format(
                    "%nINFO: Total PVC is at %4.1f%% capacity (%s/%s)", 
                    (totalClaims.getNumber().floatValue()/maxClaims.getNumber().floatValue()) * 100,
                    totalClaims.toSuffixedString(), 
                    maxClaims.toSuffixedString()
                );
            }

        }catch(ApiException apie) {
            System.err.println("Exception watching PersistentVolumeClaims");
            apie.printStackTrace();
            System.exit(1);
        }
    }

    public static void printPVCs(V1PersistentVolumeClaimList list){
        System.out.println("----- PVCs ----");
        String template = "%-16s\t%-40s\t%-6s%n";
        System.out.format(template,"Name", "Volume", "Size");
        
        for (V1PersistentVolumeClaim item : list.getItems()) {
            String name = item.getMetadata().getName();
            String volumeName = item.getSpec().getVolumeName();
            String size = item.getSpec().getResources().getRequests().get("storage");
            System.out.format(template,name, volumeName, size);
        }
    }
}
