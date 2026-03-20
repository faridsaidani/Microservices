const $ = (id) => document.getElementById(id);

function showMsg(node, type, text) {
  node.className = `msg ${type}`;
  node.textContent = text;
  node.classList.remove("hidden");
}

function clearMsg(node) {
  node.className = "msg hidden";
  node.textContent = "";
}

async function api(path, options = {}) {
  const res = await fetch(path, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  const text = await res.text();
  const body = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new Error((body && body.message) || `HTTP ${res.status}`);
  }

  return body;
}

function setRows(tbodyId, html) {
  $(tbodyId).innerHTML = html || "<tr><td colspan='10'>Aucune donnee</td></tr>";
}

async function loadProduits() {
  const data = await api("/api/v1/produits");
  const rows = data.map((p) => `
    <tr>
      <td>${p.id}</td>
      <td>${p.nom}</td>
      <td>${p.description || "-"}</td>
      <td>${p.prix}</td>
      <td><span class='badge'>${p.quantiteStock}</span></td>
    </tr>
  `).join("");
  setRows("produitsRows", rows);
}

async function createProduit() {
  const msg = $("produitMsg");
  clearMsg(msg);

  try {
    await api("/api/v1/produits", {
      method: "POST",
      body: JSON.stringify({
        nom: $("pNom").value,
        description: $("pDesc").value,
        prix: Number($("pPrix").value),
        quantiteStock: Number($("pStock").value),
      }),
    });
    showMsg(msg, "ok", "Produit cree avec succes");
    await loadProduits();
  } catch (e) {
    showMsg(msg, "err", `Erreur creation produit: ${e.message}`);
  }
}

async function loadCommandes() {
  const data = await api("/api/v1/commandes");
  const rows = data.map((c) => `
    <tr>
      <td>${c.id}</td>
      <td>${c.dateCommande || "-"}</td>
      <td><span class='badge'>${c.statut}</span></td>
      <td>${c.montantTotal}</td>
      <td>${(c.lignes || []).length}</td>
    </tr>
  `).join("");
  setRows("commandesRows", rows);
}

async function createCommande() {
  const msg = $("commandeMsg");
  clearMsg(msg);

  try {
    await api("/api/v1/commandes", {
      method: "POST",
      body: JSON.stringify({
        lignes: [{
          produitId: Number($("cProduitId").value),
          quantite: Number($("cQuantite").value),
        }],
      }),
    });
    showMsg(msg, "ok", "Commande creee avec succes");
    await loadCommandes();
  } catch (e) {
    showMsg(msg, "err", `Erreur creation commande: ${e.message}`);
  }
}

async function loadPaiements() {
  const data = await api("/api/v1/paiements");
  const rows = data.map((p) => `
    <tr>
      <td>${p.id}</td>
      <td>${p.commandeId}</td>
      <td>${p.montant}</td>
      <td>${p.modePaiement}</td>
      <td><span class='badge'>${p.statut}</span></td>
      <td>${p.datePaiement || "-"}</td>
    </tr>
  `).join("");
  setRows("paiementsRows", rows);
}

async function createPaiement() {
  const msg = $("paiementMsg");
  clearMsg(msg);

  try {
    await api("/api/v1/paiements", {
      method: "POST",
      body: JSON.stringify({
        commandeId: Number($("payCommandeId").value),
        modePaiement: $("payMode").value,
      }),
    });
    showMsg(msg, "ok", "Paiement cree avec succes");
    await loadPaiements();
  } catch (e) {
    showMsg(msg, "err", `Erreur creation paiement: ${e.message}`);
  }
}

async function checkHealth() {
  const out = $("healthOutput");
  try {
    const health = await api("/actuator/health");
    out.textContent = JSON.stringify(health, null, 2);
  } catch (e) {
    out.textContent = `Erreur health: ${e.message}`;
  }
}

function setupTabs() {
  const tabs = Array.from(document.querySelectorAll(".tab"));
  tabs.forEach((btn) => {
    btn.addEventListener("click", () => {
      tabs.forEach((t) => t.classList.remove("active"));
      btn.classList.add("active");
      document.querySelectorAll(".panel").forEach((p) => p.classList.add("hidden"));
      document.getElementById(btn.dataset.tab).classList.remove("hidden");
    });
  });
}

function wireActions() {
  $("createProduit").addEventListener("click", createProduit);
  $("reloadProduits").addEventListener("click", () => loadProduits().catch(() => {}));

  $("createCommande").addEventListener("click", createCommande);
  $("reloadCommandes").addEventListener("click", () => loadCommandes().catch(() => {}));

  $("createPaiement").addEventListener("click", createPaiement);
  $("reloadPaiements").addEventListener("click", () => loadPaiements().catch(() => {}));

  $("checkHealth").addEventListener("click", checkHealth);
}

async function init() {
  setupTabs();
  wireActions();

  await Promise.allSettled([
    loadProduits(),
    loadCommandes(),
    loadPaiements(),
  ]);
}

init();
