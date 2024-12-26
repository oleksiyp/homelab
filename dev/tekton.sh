# FIXME temporary
#kubectl apply --filename https://storage.googleapis.com/tekton-releases/pipeline/latest/release.yaml
#kubectl apply --filename https://storage.googleapis.com/tekton-releases/triggers/latest/release.yaml
#kubectl apply --filename https://storage.googleapis.com/tekton-releases/dashboard/latest/release.yaml

kubectl apply --filename - <<DOC
apiVersion: triggers.tekton.dev/v1beta1
kind: EventListener
metadata:
  name: gitea-event-listener
spec:
  serviceAccountName: pipeline
  triggers:
    - name: gitea-trigger
      bindings:
        - ref: gitea-trigger-binding
      template:
        ref: gitea-trigger-template
DOC