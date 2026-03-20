param(
    [string]$Namespace = "default"
)

$ErrorActionPreference = "Continue"

$releases = @(
    "paiement-service",
    "commande-service",
    "catalogue-service",
    "gateway-service",
    "discovery-service",
    "config-service"
)

if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
    throw "Commande requise introuvable: helm"
}

foreach ($release in $releases) {
    Write-Host "[INFO] helm uninstall $release --namespace $Namespace"
    helm uninstall $release --namespace $Namespace | Out-Null
}

Write-Host "[OK] Releases Helm supprimees (si presentes)"
helm list --namespace $Namespace
kubectl get pods --namespace $Namespace
