import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.Pair;
import io.kubernetes.client.auth.*;
import io.kubernetes.client.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVCList {
    public static void main(String[] args) throws IOException, ApiException{
        // K8S_NAMESPACE defaults to empty
        String namespace = System.getenv("K8S_NAMESPACE");

        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        // fallback to default namespace
        if (namespace == null || "".equals(namespace)) {
            namespace = "default";
        }

        //client.setDebugging(true);

        CoreV1Api api = new CoreV1Api(client);
        V1PersistentVolumeClaimList list = api.listNamespacedPersistentVolumeClaim(namespace, null, null, null, null, null, null, null, null, null);

        String apiServer = client.getBasePath();
        System.out.format("%nconnecting to API server %s %n%n", apiServer);
        System.out.println("----- PVCs ----");
        String template = "%-16s\t%-40s\t%-6s%n";
        System.out.format(template,"Name", "Volume", "Size");
        
        for (V1PersistentVolumeClaim item : list.getItems()) {
            String name = item.getMetadata().getName();
            String volumeName = item.getSpec().getVolumeName();
            String size = item.getSpec().getResources().getRequests().get("storage");
            System.out.format(template,name, volumeName, size);
        }

        System.out.println();
    }
}
