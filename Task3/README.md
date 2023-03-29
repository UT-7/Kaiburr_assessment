Task 3 - Create Rest API server and mongodb in Kubernetes

We have installed docker and minikube cluster for Kubernetes and Helm charts
Added MongoDB and our Java Rest API server as pods


- Install minikube
	curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube_latest_amd64.deb
	dpkg -i minikube_latest_amd64.deb
	minikube start --driver=docker

- Install kubectl
	curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
	sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

- Install Helm-
1) curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
2) sudo apt-get install apt-transport-https --yes
3) echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
4) sudo apt update
5) sudo apt install helm
6) helm repo add stable https://charts.helm.sh/stable

- Create MongoDB in Kubernetes using Helm charts-
1) helm repo add bitnami https://charts.bitnami.com/bitnami
2) helm install mongodb-dev bitnami/mongodb
3) Mongodb installed here uses persistent data
4) MongoDB can be accessed on the following DNS name(s) and ports from within cluster by our Rest API server:
    - mongodb-dev
5) To get the root password run:
   export MONGODB_ROOT_PASSWORD=$(kubectl get secret --namespace default mongodb-dev -o jsonpath="{.data.mongodb-root-password}" | base64 -d)

- Create MongoDB service to expose it for testing and root user password change
1) kubectl apply -f mongo-nodeport-svc.yaml
2) Mongodb server is available on port 32000 and IP of minikube cluster
3) Get minikube IP
	Run -> minikube ip
	192.168.49.2
4) Connect mongodb
	Run -> mongo --host 192.168.49.2 --port 32000 --authenticationDatabase admin  -u root -p $MONGODB_ROOT_PASSWORD
5) Change root user password
	> db = db.getSiblingDB('admin')
	> db.changeUserPassword("root", "password098")
	> exit
6) We will use this password in our Rest Api server to connect mongodb

- Deploy Rest API server application in Kubernetes
1) Set minikube docker env. Run
	eval $(minikube docker-env)
2) Check docker for kubernetes containers, Run
	docker ps
3) Build out Rest API server docker image, check our files
	- Dockerfile
	- pom.xml
	- src/main/java/server.java
	Run
	docker build -t restserver/server .
4) Now our server image is available in docker
	Following is output of command - docker images:
		utkarsh@utkarsh:~/restserver$ docker images
			REPOSITORY                                TAG                  IMAGE ID       CREATED         SIZE
			restserver/server                         latest               844bb6a8df34   2 hours ago     840MB
			bitnami/mongodb                           6.0.5-debian-11-r1   59b7f94229a3   9 days ago      573MB
			
5) Creat Rest API server pods, deployment and service
	App is running on port 8080, Run
	kubectl apply server.yaml
6) This will also expose service app using service on port 3000
   Check pods created, Run
	utkarsh@utkarsh:~$ kubectl get pods
		NAME                          READY   STATUS    RESTARTS      AGE
		mongodb-dev-54f4bc7b9-ccbwx   1/1     Running   1 (55s ago)   11h
		restserver-7fd4dd986-wp7bk    1/1     Running   0             12h
7) Check Get request using curl
	utkarsh@utkarsh:~/restserver$ curl 192.168.49.2:30000/servers, Run
		[{"name":"mywin","id":"124","language":"py","framework":"pyt"},{"name":"my ubuntu","id":"125","language":"py","framework":"pyt"}]
8) Test server creation using curl, Run
    - utkarsh@utkarsh:~/restserver$ curl 192.168.49.2:30000/servers -X PUT -d "{'name':'my-rhel','id':'126','language':'py','framework':'pyt'}"
		Server created	
	- utkarsh@utkarsh:~/restserver$ curl 192.168.49.2:30000/servers
		[{"name":"mywin","id":"124","language":"py","framework":"pyt"},{"name":"my ubuntu","id":"125","language":"py","framework":"pyt"},{"name":"my-rhel","id":"126","language":"py","framework":"pyt"}]
8) We can also use the postman scripts in task 1 to test the apis

