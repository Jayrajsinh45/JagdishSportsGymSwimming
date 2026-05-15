const STORAGE_KEY = "jagdishSportsMembers";
const CATEGORIES = {
  GYM: "Gym",
  SWIMMING: "Swimming"
};

const state = {
  screen: "home",
  category: null,
  reportFilter: "All",
  editingMemberId: null
};

const elements = {
  app: document.querySelector("#app"),
  title: document.querySelector("#screenTitle"),
  backButton: document.querySelector("#backButton"),
  homeTab: document.querySelector("#homeTab"),
  reportTab: document.querySelector("#reportTab"),
  dialog: document.querySelector("#memberDialog"),
  form: document.querySelector("#memberForm"),
  dialogTitle: document.querySelector("#dialogTitle"),
  dialogCategory: document.querySelector("#dialogCategory"),
  fullName: document.querySelector("#fullName"),
  phoneNumber: document.querySelector("#phoneNumber"),
  startDate: document.querySelector("#startDate"),
  endDate: document.querySelector("#endDate"),
  feesPaid: document.querySelector("#feesPaid"),
  formError: document.querySelector("#formError"),
  deleteMemberButton: document.querySelector("#deleteMemberButton"),
  cancelFormButton: document.querySelector("#cancelFormButton"),
  closeDialogButton: document.querySelector("#closeDialogButton")
};

function loadMembers() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY)) || [];
  } catch {
    return [];
  }
}

function saveMembers(members) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(members));
}

function todayIso() {
  return toIsoDate(new Date());
}

function addMonths(date, months) {
  const next = new Date(date);
  next.setMonth(next.getMonth() + months);
  return next;
}

function toIsoDate(date) {
  const copy = new Date(date);
  copy.setMinutes(copy.getMinutes() - copy.getTimezoneOffset());
  return copy.toISOString().slice(0, 10);
}

function parseLocalDate(value) {
  return new Date(`${value}T00:00:00`);
}

function formatDate(value) {
  return parseLocalDate(value).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric"
  });
}

function formatRupees(value) {
  return `₹${Number(value || 0).toLocaleString("en-IN")}`;
}

function daysUntil(endDate) {
  const start = parseLocalDate(todayIso());
  const end = parseLocalDate(endDate);
  return Math.ceil((end - start) / 86400000);
}

function getStatus(member, soonDays = 5) {
  const days = daysUntil(member.endDate);
  if (days < 0) {
    return { key: "expired", label: "Expired", icon: "✕" };
  }
  if (days <= soonDays) {
    return { key: "soon", label: "Expiring Soon", icon: "!" };
  }
  return { key: "active", label: "Active", icon: "✓" };
}

function setScreen(screen, options = {}) {
  state.screen = screen;
  Object.assign(state, options);
  render();
}

function updateChrome() {
  const isHomeArea = state.screen === "home" || state.screen === "members";
  elements.homeTab.classList.toggle("active", isHomeArea);
  elements.reportTab.classList.toggle("active", state.screen === "report");
  elements.backButton.classList.toggle("hidden", state.screen === "home" || state.screen === "report");

  if (state.screen === "members") {
    elements.title.textContent = `${state.category} Members`;
    return;
  }
  if (state.screen === "report") {
    elements.title.textContent = "Report";
    return;
  }
  elements.title.textContent = "Jagdish Sports Gym and Swimming";
}

function render() {
  updateChrome();
  if (state.screen === "members") {
    renderMembers();
    return;
  }
  if (state.screen === "report") {
    renderReport();
    return;
  }
  renderHome();
}

function renderHome() {
  elements.app.innerHTML = `
    <section class="home-grid">
      <p class="intro">Manage gym and swimming memberships from the browser. Data is saved in this browser only.</p>
      <button class="category-card gym" data-category="${CATEGORIES.GYM}">
        <span class="category-icon" aria-hidden="true">🏋</span>
        <span>
          <h2>Gym Members</h2>
          <p>Add, edit, and review gym memberships</p>
        </span>
      </button>
      <button class="category-card swimming" data-category="${CATEGORIES.SWIMMING}">
        <span class="category-icon" aria-hidden="true">≈</span>
        <span>
          <h2>Swimming Members</h2>
          <p>Track pool memberships and fees</p>
        </span>
      </button>
    </section>
  `;

  elements.app.querySelectorAll("[data-category]").forEach((button) => {
    button.addEventListener("click", () => {
      setScreen("members", { category: button.dataset.category });
    });
  });
}

function renderMembers() {
  const members = loadMembers()
    .filter((member) => member.category === state.category)
    .sort((a, b) => a.endDate.localeCompare(b.endDate) || a.fullName.localeCompare(b.fullName));

  elements.app.innerHTML = `
    <section>
      <div class="list-header">
        <div>
          <h2>${state.category} Members</h2>
          <p class="muted">${members.length} saved member${members.length === 1 ? "" : "s"}</p>
        </div>
      </div>
      <div class="member-list">
        ${
          members.length
            ? members.map((member) => memberCard(member)).join("")
            : emptyState(`No ${state.category} members yet`, "Tap the add button to create the first membership.")
        }
      </div>
      <button class="fab" id="addMemberFab" type="button" aria-label="Add member">+</button>
    </section>
  `;

  elements.app.querySelector("#addMemberFab").addEventListener("click", () => openMemberDialog());
  elements.app.querySelectorAll("[data-member-id]").forEach((card) => {
    card.addEventListener("click", () => openMemberDialog(card.dataset.memberId));
  });
}

function memberCard(member, soonDays = 5) {
  const status = getStatus(member, soonDays);
  return `
    <button class="member-card" data-member-id="${member.id}" type="button">
      <span class="member-top">
        <span>
          <span class="member-name">${escapeHtml(member.fullName)}</span>
          <span class="muted">☎ ${escapeHtml(member.phoneNumber)}</span>
        </span>
        <span class="badge ${status.key}">${status.icon} ${status.label}</span>
      </span>
      <span class="date-row">
        <span class="date-block">
          <span>Start</span>
          <span>${formatDate(member.startDate)}</span>
        </span>
        <span class="date-block">
          <span>End</span>
          <span>${formatDate(member.endDate)}</span>
        </span>
      </span>
      <span class="member-bottom">
        <span class="chip">${member.category === CATEGORIES.GYM ? "🏋" : "≈"} ${member.category}</span>
        <span class="fees">${formatRupees(member.feesPaid)}</span>
      </span>
    </button>
  `;
}

function renderReport() {
  const members = loadMembers();
  const gymCount = members.filter((member) => member.category === CATEGORIES.GYM).length;
  const swimmingCount = members.filter((member) => member.category === CATEGORIES.SWIMMING).length;
  const activeCount = members.filter((member) => daysUntil(member.endDate) >= 0).length;
  const expiredCount = members.filter((member) => daysUntil(member.endDate) < 0).length;
  const expiringSoonCount = members.filter((member) => {
    const days = daysUntil(member.endDate);
    return days >= 0 && days <= 7;
  }).length;
  const totalFees = members.reduce((sum, member) => sum + Number(member.feesPaid || 0), 0);
  const filtered = filterReportMembers(members);

  elements.app.innerHTML = `
    <section>
      <div class="report-header">
        <h2>Membership Summary</h2>
      </div>
      <div class="stat-grid">
        ${statCard("Total Members", `Gym ${gymCount} + Swimming ${swimmingCount} = ${members.length}`)}
        ${statCard("Active", activeCount)}
        ${statCard("Expired", expiredCount)}
        ${statCard("Expiring Soon", expiringSoonCount)}
        ${statCard("Fees Collected", formatRupees(totalFees))}
      </div>
      ${chartCard(gymCount, swimmingCount)}
      <div class="filter-row" aria-label="Report filters">
        ${["All", "This Month", "Expired"].map((filter) => `
          <button class="chip ${state.reportFilter === filter ? "active" : ""}" data-filter="${filter}" type="button">${filter}</button>
        `).join("")}
      </div>
      <div class="member-list">
        ${
          filtered.length
            ? filtered.map((member) => memberCard(member, 7)).join("")
            : emptyState("No matching members", "Try another filter or add members from Home.")
        }
      </div>
    </section>
  `;

  elements.app.querySelectorAll("[data-filter]").forEach((button) => {
    button.addEventListener("click", () => {
      state.reportFilter = button.dataset.filter;
      renderReport();
    });
  });

  elements.app.querySelectorAll("[data-member-id]").forEach((card) => {
    card.addEventListener("click", () => {
      const member = loadMembers().find((item) => item.id === card.dataset.memberId);
      if (member) {
        setScreen("members", { category: member.category });
        openMemberDialog(member.id);
      }
    });
  });
}

function filterReportMembers(members) {
  if (state.reportFilter === "Expired") {
    return members.filter((member) => daysUntil(member.endDate) < 0);
  }
  if (state.reportFilter === "This Month") {
    const month = todayIso().slice(0, 7);
    return members.filter((member) => member.startDate.slice(0, 7) === month);
  }
  return members;
}

function statCard(label, value) {
  return `
    <article class="stat-card">
      <span>${label}</span>
      <strong>${value}</strong>
    </article>
  `;
}

function chartCard(gymCount, swimmingCount) {
  const total = gymCount + swimmingCount;
  const gymWidth = total ? (gymCount / total) * 100 : 0;
  const swimmingWidth = total ? (swimmingCount / total) * 100 : 0;
  return `
    <article class="chart-card">
      <h3>Gym vs Swimming Split</h3>
      ${
        total
          ? `
            <div class="bar" aria-label="Gym vs Swimming member split">
              <span class="bar-segment gym" style="width: ${gymWidth}%"></span>
              <span class="bar-segment swimming" style="width: ${swimmingWidth}%"></span>
            </div>
            <div class="legend">
              <span><i class="dot gym"></i>Gym: ${gymCount}</span>
              <span><i class="dot swimming"></i>Swimming: ${swimmingCount}</span>
            </div>
          `
          : `<p class="muted">No member data yet</p>`
      }
    </article>
  `;
}

function emptyState(title, message) {
  return `
    <article class="empty-state">
      <h2>${title}</h2>
      <p class="muted">${message}</p>
    </article>
  `;
}

function openMemberDialog(memberId = null) {
  const members = loadMembers();
  const member = members.find((item) => item.id === String(memberId));
  state.editingMemberId = member?.id || null;
  elements.formError.textContent = "";
  elements.dialogCategory.textContent = state.category;
  elements.dialogTitle.textContent = member ? "Edit Member" : "Add Member";
  elements.deleteMemberButton.classList.toggle("hidden", !member);
  elements.fullName.value = member?.fullName || "";
  elements.phoneNumber.value = member?.phoneNumber || "";
  elements.startDate.value = member?.startDate || todayIso();
  elements.endDate.value = member?.endDate || toIsoDate(addMonths(new Date(), 1));
  elements.feesPaid.value = member?.feesPaid || "";
  elements.dialog.showModal();
  elements.fullName.focus();
}

function closeMemberDialog() {
  elements.dialog.close();
  state.editingMemberId = null;
}

function saveMemberFromForm() {
  const fullName = elements.fullName.value.trim();
  const phoneNumber = elements.phoneNumber.value.trim();
  const startDate = elements.startDate.value;
  const endDate = elements.endDate.value;
  const feesPaid = Number(elements.feesPaid.value);

  if (!fullName || !phoneNumber || !startDate || !endDate || Number.isNaN(feesPaid)) {
    elements.formError.textContent = "Please fill all fields.";
    return;
  }

  if (parseLocalDate(endDate) < parseLocalDate(startDate)) {
    elements.formError.textContent = "End date cannot be before start date.";
    return;
  }

  const members = loadMembers();
  const existingIndex = members.findIndex((member) => member.id === state.editingMemberId);
  const nextMember = {
    id: state.editingMemberId || crypto.randomUUID(),
    fullName,
    phoneNumber,
    startDate,
    endDate,
    feesPaid,
    category: state.category,
    createdAt: existingIndex >= 0 ? members[existingIndex].createdAt : Date.now()
  };

  if (existingIndex >= 0) {
    members[existingIndex] = nextMember;
  } else {
    members.push(nextMember);
  }

  saveMembers(members);
  closeMemberDialog();
  render();
}

function deleteCurrentMember() {
  if (!state.editingMemberId) {
    return;
  }

  const member = loadMembers().find((item) => item.id === state.editingMemberId);
  if (!member) {
    return;
  }

  const confirmed = window.confirm(`Delete ${member.fullName}?`);
  if (!confirmed) {
    return;
  }

  saveMembers(loadMembers().filter((item) => item.id !== state.editingMemberId));
  closeMemberDialog();
  render();
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

elements.homeTab.addEventListener("click", () => setScreen("home"));
elements.reportTab.addEventListener("click", () => setScreen("report"));
elements.backButton.addEventListener("click", () => setScreen("home"));
elements.cancelFormButton.addEventListener("click", closeMemberDialog);
elements.closeDialogButton.addEventListener("click", closeMemberDialog);
elements.deleteMemberButton.addEventListener("click", deleteCurrentMember);
elements.form.addEventListener("submit", (event) => {
  event.preventDefault();
  saveMemberFromForm();
});

render();
