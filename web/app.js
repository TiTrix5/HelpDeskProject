// FRONTEND: логика страницы. Общается с сервером через /api/tickets.

const STATUS_NAMES = { NEW: "Новая", IN_PROGRESS: "В работе", DONE: "Готово" };
let tickets = [];
let filter = "ALL";

const $ = (id) => document.getElementById(id);

// Защита от внедрения HTML в текст заявок
function escapeHtml(s) {
  const div = document.createElement("div");
  div.textContent = s;
  return div.innerHTML;
}

async function loadTickets() {
  const res = await fetch("/api/tickets");
  tickets = await res.json();
  render();
}

function render() {
  const shown = tickets.filter((t) => filter === "ALL" || t.status === filter).slice().reverse();
  if (shown.length === 0) {
    $("list").innerHTML = '<div class="empty">Заявок пока нет</div>';
    return;
  }
  $("list").innerHTML = shown.map((t) => `
    <div class="ticket ${t.status}">
      <h3>#${t.id} ${escapeHtml(t.title)}</h3>
      <div class="meta">${escapeHtml(t.author)} · ${t.createdAt}</div>
      <div class="desc">${escapeHtml(t.description)}</div>
      <select data-id="${t.id}">
        ${Object.keys(STATUS_NAMES).map((s) =>
          `<option value="${s}" ${s === t.status ? "selected" : ""}>${STATUS_NAMES[s]}</option>`).join("")}
      </select>
    </div>`).join("");
}

async function createTicket() {
  $("error").textContent = "";
  const body = new URLSearchParams({
    author: $("author").value,
    title: $("title").value,
    description: $("description").value,
  });
  const res = await fetch("/api/tickets", { method: "POST", body });
  if (!res.ok) {
    const data = await res.json();
    $("error").textContent = data.error || "Ошибка";
    return;
  }
  $("title").value = "";
  $("description").value = "";
  loadTickets();
}

async function changeStatus(id, status) {
  await fetch(`/api/tickets/${id}/status`, { method: "POST", body: new URLSearchParams({ status }) });
  loadTickets();
}

$("createBtn").addEventListener("click", createTicket);

$("list").addEventListener("change", (e) => {
  if (e.target.tagName === "SELECT") changeStatus(e.target.dataset.id, e.target.value);
});

document.querySelector(".filters").addEventListener("click", (e) => {
  if (e.target.dataset.filter) {
    filter = e.target.dataset.filter;
    document.querySelectorAll(".filters button").forEach((b) => b.classList.toggle("active", b === e.target));
    render();
  }
});

loadTickets();
