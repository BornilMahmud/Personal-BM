# Personal-BM

**Every Taka. Every Transaction. Total Control.**

Personal BM is a complete, private, cloud-synced personal financial management application designed with a **Minimal Light Fintech** interface and full support for Bangladeshi Taka (`BDT` / `৳`), local digital wallets, and real-time analytics.

---

## Key Features

- **Minimal Light Fintech UI/UX & Optional Dark Mode**
  - Clean, high-contrast Minimal Light design (`#F8FAFC` background, `#FFFFFF` cards, `#0F172A` navy typography, `#2563EB` primary accents, `#059669` income emerald, `#E11D48` expense crimson) with an optional **Premium Dark** theme.
  - Purpose-built responsive layouts for **Smartphones** (5-tab bottom navigation, floating `+` action button, quick actions strip, and compact transaction cards) and **Tablets / Desktops** (collapsible left sidebar, top search & date bar, 12-column dashboard grid, and multi-column transaction table).
- **Exact Paisa Arithmetic (`1 BDT = 100 Paisa`)**
  - All monetary values and account balances use 64-bit integer Paisa (`Long`) arithmetic to eliminate floating-point rounding drift.
- **Pre-Loaded Bangladeshi Accounts & Wallets**
  - Automatic first-login seeding for **Cash**, **Bank Account**, **bKash**, **Nagad**, **Rocket**, **Upay**, and **Other**, plus custom wallet creation and atomic account-to-account transfers.
- **13 Integrated Financial Modules**
  1. **Overview Dashboard**: Hero Total Balance card with blue sparkline, Income/Expense/Savings cards, Quick Actions, Cash Flow chart, Category Donut chart, Budget Progress, Upcoming Bills, and Recent Transactions.
  2. **Transactions**: Search, filter by type/date/category/account, sort, pagination, duplicate, edit, and delete with atomic balance reconciliation.
  3. **Income Management**: Source breakdown, custom income categories (English & বাংলা), and history.
  4. **Expense Management**: Category spending distribution, custom expense categories, and history.
  5. **Accounts & Wallets**: Live wallet balances, transfers, per-account transaction ledger, and safe delete protection.
  6. **Interactive Analytics**: Area/Line, Grouped Bar, and Donut charts across **Daily**, **Weekly**, **Monthly**, and **Yearly** horizons.
  7. **Budgets**: Monthly overall and category budgets with **75%**, **90%**, and **100%** threshold alerts.
  8. **Savings Goals**: Target dates, progress tracking, and contributions without double-counting as income.
  9. **Recurring Transactions**: Daily, Weekly, Monthly, and Yearly schedules with idempotent auto-posting.
  10. **Loans & Debts**: Borrowed vs. Lent tracking, partial/full repayments, and terminal `PAID` status locking.
  11. **Financial Calendar**: Monthly calendar view with daily income, expense, and recurring due markers.
  12. **Reports & Exports**: 8 statement types with native **CSV** and multi-page **A4 PDF** generation.
  13. **Settings & Backup**: Profile, BDT (`৳`), `Asia/Dhaka` timezone, English / বাংলা language toggle, Light/Dark theme toggle, and full JSON backup export.

---

## Tech Stack

- **UI Framework**: Jetpack Compose + Material Design 3
- **Language**: Kotlin
- **Authentication**: Firebase Authentication with Google Sign-In (`CredentialManager`)
- **Database**: Cloud Firestore with Zero-Trust user-isolated security rules (`firestore.rules`)
- **Typography**: Bundled `Inter` and `JetBrains Mono` (tabular numerals)

---

## Pushing to GitHub (`BornilMahmud/Personal-BM`)

### Option 1: Directly from Google AI Studio
1. Click the **GitHub / Export** icon (or the top-right menu) in the AI Studio header.
2. Select **Push to GitHub**, authorize your `BornilMahmud` GitHub account, and select or create **`Personal-BM`**.

### Option 2: From Your Local Terminal
1. Download the project ZIP from the AI Studio menu (**Download ZIP**) and extract it.
2. Open a terminal in the extracted folder and run:

```bash
git init
git add .
git commit -m "first commit: Personal BM Minimal Light Fintech App"
git branch -M main
git remote add origin https://github.com/BornilMahmud/Personal-BM.git
git push -u origin main
```
