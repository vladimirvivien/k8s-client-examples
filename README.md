# k8s-client-examples
Building stuff with the Kubernetes API

Kubernetes is a formidable platform on (and with) which you can create all sorts of tools and clients.  Fortunately, there are many options when it comes to programming against the Kubernetes APIs. Unfortunately, these options can be overwhelming with a multitude of APIs which can leave potential developers with no clear directions.  

This repo highlights the extensibility of Kubernetes as a platform.  It provides different examples and walkthroughs of the different options that are available from building simple clients to more complex extension of Kubernetes itself. While the concepts presented here can be applied with any language that can access the Kubernetes API, the discussion and code sample focus on the Go programming language.

### Examples 
In this repo you will find a contrived example of a PVC watcher that monitors the claimed sizes of installed PVCs:

Go
  * [go/pvcwatch](./go/pvcwatch) - A simple implementation using a `Watcher`
  * [go/pvcwatch](./go/pvcwatch-ctl) - Go implementation using a controller

Java
  * [java](./java) - A simple implementation that uses `Watch` object

Python
  * [python](./python) - A simple implementation that uses the Python `Watch` object
