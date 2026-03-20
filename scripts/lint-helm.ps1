$ErrorActionPreference = "Stop"

if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
    throw "Commande requise introuvable: helm"
}

$charts = @(
    "config-service",
    "discovery-service",
    "gateway-service",
    "catalogue-service",
    "commande-service",
    "paiement-service"
)

foreach ($chart in $charts) {
    Write-Host "[INFO] helm lint ./charts/$chart"
    helm lint "./charts/$chart"
}

Write-Host "[OK] Tous les charts Helm sont valides"
