# pvcwatch
`pvcwatch` is a CLI-based tool which watches the total claimed persistent storage capacity in a cluster.

## Requirements
This example requires:

* the `dep` [dependency management tool](https://github.com/golang/dep)
* Kubernetes [client-go](https://github.com/kubernetes/client-go)

Read the note on [versioning and compatibilty](https://github.com/kubernetes/client-go#versioning), of the Kubernetes client-go codebase, to determine your dependency for the targeted Kubernetes server.  

Optionally, you can update the `Gopkg.toml` file to target a preferred Kubernetes version.  If you do so, remember to run `dep ensure` and `dep status` to ensure you are pointing to the desired dependencies.

## Build/Run
Build the code in this package:

```shell
$> cd pvcwatch
$> go build .
```

The easiest way to test the binary is to run it on environment that already has `kubectl` installed and configured.  Next, review the supported parameters

```shell
$> ./pvcwatch --help

Usage of ./pvcwatch:
  -f string
    	Field selector
  -kubeconfig string
    	kubeconfig file (default "/Users/<username>/.kube/config")
  -l string
    	Label selector
  -max-claims string
    	Maximum total claims to watch (default "200Gi")
  -namespace string
    	namespace
...
```
Next we can start the tool with all default parameters:
```shell

$> ./pvcwatch

```