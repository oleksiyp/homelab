kubectl apply --filename - <<DOC
kind: Secret
apiVersion: v1
metadata:
  name: gitea-actions
stringData:
  secret: RZ0nL9cTl8pILLYVJUtewZ8GVN5Q00pBm7ePYFep
DOC
helm repo add gitea-charts https://dl.gitea.com/charts/
helm repo update gitea-charts
helm upgrade --values - --install gitea gitea-charts/gitea <<DOC
actions:
  enabled: true
  existingSecret: gitea-actions
  existingSecretKey: secret
DOC
