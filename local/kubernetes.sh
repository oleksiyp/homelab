sudo /usr/local/bin/k3s-uninstall.sh || echo -n
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --disable servicelb" sh -
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
while ! kubectl get deployment -n kube-system coredns local-path-provisioner metrics-server; do sleep 1; done
kubectl rollout status deployment -n kube-system coredns local-path-provisioner metrics-server --timeout=90s
# gen 1 stuff
#helmfile sync
#while ! kubectl get cm -n infra internal-ca-tls; do sleep 1; done
#kubectl get cm -n infra internal-ca-tls -o json | jq '.data["ca.crt"]' -r | sudo tee /etc/ssl/certs/internal-ca.pem
#sudo update-ca-certificates
#sudo systemctl restart k3s
#sudo systemctl restart docker