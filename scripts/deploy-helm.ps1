param(
    [string]$Namespace = "default",
    [string]$DockerHubUser = "",
    [switch]$UseMinikubeLocalImages,
    [string]$ImageTag = "latest"
)

$ErrorActionPreference = "Stop"

$services = @(
    "config-service",
    "discovery-service",
    "gateway-service",
    "catalogue-service",
    "commande-service",
    "paiement-service"
)

function Assert-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Commande requise introuvable: $Name"
    }
}

Assert-Command "helm"
Assert-Command "kubectl"

if ($UseMinikubeLocalImages) {
    Assert-Command "minikube"
    Assert-Command "docker"

    $statusJson = minikube status --output=json | ConvertFrom-Json
    if ($statusJson.Host -ne "Running") {
        throw "Minikube n'est pas demarre. Lance: minikube start"
    }

    foreach ($svc in $services) {
        $imageRef = "$svc`:$ImageTag"
        Write-Host "[INFO] Build image locale $imageRef"
        docker build -t $imageRef "./$svc"
        Write-Host "[INFO] Chargement Minikube image $imageRef"
        minikube image load $imageRef
    }
}

$commonArgs = @("--namespace", $Namespace, "--create-namespace")

if ($DockerHubUser -ne "") {
    Write-Host "[INFO] Deployment avec images Docker Hub: $DockerHubUser/*:$ImageTag"
}
elseif ($UseMinikubeLocalImages) {
    Write-Host "[INFO] Deployment avec images locales Minikube (pullPolicy=Never)"
}
else {
    Write-Host "[INFO] Deployment avec repositories par defaut des charts"
}

foreach ($svc in $services) {
    $chartPath = "./charts/$svc"
    $release = $svc
    $args = @("upgrade", "--install", $release, $chartPath) + $commonArgs

    if ($DockerHubUser -ne "") {
        $args += @("--set", "image.repository=$DockerHubUser/$svc", "--set", "image.tag=$ImageTag", "--set", "image.pullPolicy=IfNotPresent")
    }
    elseif ($UseMinikubeLocalImages) {
        $args += @("--set", "image.repository=$svc", "--set", "image.tag=$ImageTag", "--set", "image.pullPolicy=Never")
    }

    Write-Host "[INFO] helm $($args -join ' ')"
    helm @args

    if ($LASTEXITCODE -ne 0) {
        throw "Echec deployment Helm pour $svc"
    }

    Start-Sleep -Seconds 5
}

Write-Host "[OK] Deploiement Helm termine"
Write-Host "[INFO] Verification rapide"
helm list --namespace $Namespace
kubectl get pods --namespace $Namespace
kubectl get services --namespace $Namespace
