# k8s-client-examples
Building stuff with the Kubernetes API

Kubernetes is a formidable platform on (and with) which you can create all sorts of tools and clients.  Fortunately, there are many options when it comes to programming against the Kubernetes APIs. Unfortunately, these options can be overwhelming with a multitude of APIs which can leave potential developers with no clear directions.  

This repo highlights the extensibility of Kubernetes as a platform.  It provides different examples and walkthroughs of the different options that are available from building simple clients to more complex extension of Kubernetes itself. While the concepts presented here can be applied with any language that can access the Kubernetes API, the discussion and code sample focus on the Go programming language.

### Examples 
In this repo you will find the following examples:

* [pvcwatch](./pvcwatch) - a contrived, but functional, tool that is used to observe and react to events in a running cluster