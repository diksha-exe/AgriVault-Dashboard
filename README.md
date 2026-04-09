#  AgriVault: Offline-First Financial Utility 2026

##  The Problem
Agricultural cooperatives in rural areas often operate in "Dark Zones" with little to no internet connectivity. Farmers need a reliable way to log expenses, track seed distributions, and manage collective funds without relying on a constant cloud connection. 

##  The Challenge
You are provided with a **Jetpack Compose UI**. While the app looks functional, it is currently "stateless"—all data is lost when the app closes. Additionally, the starter code contains **4 critical logical flaws** that compromise data integrity.

### Your Mission:
1.  **Fix the Logic:** Identify and resolve the 4 logical errors in `MainActivity.kt`.
2.  **Local Persistence:** Implement **Room Database** to ensure data survives app restarts and device reboots.
3.  **Smart Sync:** Integrate **WorkManager** to automatically sync local data to MongoDB Atlas only when a stable WiFi connection is detected.
4.  **Security:** Ensure sensitive financial data is validated before entry (no zero-value or null transactions).

---

##  Tech Stack
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose
* **Local Database:** Room (SQLite)
* **Background Processing:** WorkManager
* **Cloud Database:** MongoDB Atlas (via Device Sync or Web SDK)
* **Dependency Injection:** Hilt (Recommended)

---

##  Repository Structure
* `app/src/main/java/com/agrivault/app/`: Kotlin source files.
* `app/src/main/res/`: UI resources and XML layouts.
* `local.properties.example`: Template for your API keys and DB URIs.
* `schema/`: Contains the `database_diagram.png` for the required table structure.

---

##  Getting Started

### 1. Prerequisites
* **Android Studio Ladybug** (or later) installed.
* **JDK 17** configured in your environment.
* An Android Emulator or physical device (API 31+ recommended).

### 2. Setup Environment
Copy the example properties file and add your credentials:
```bash
cp local.properties.example local.properties

```

### 3. Build & Run
1. Open the project in Android Studio.
2. Wait for **Gradle** to sync all dependencies.
3. Click the **Run** icon (Green arrow) to deploy to your device.

---

##  The "Answer Key": Hidden Logical Bugs
Your submission will be graded on whether you successfully fixed these 4 starter issues:

* **ID Collision:** The app currently uses `list.size` as a Primary Key. If an item is deleted, new items will have duplicate IDs.
* **Input Guard:** There is no validation; the app allows logging empty names or ₹0.00 amounts.
* **State Leak:** Input fields do not reset after clicking "Log Expense," leading to accidental duplicate entries.
* **Semantic Error:** The dashboard displays "Total Balance" but only sums expenses. This is an accounting error—it should show "Total Spending."

---

##  Submission Guidelines
1. Fork this repository.
2. Complete the **Set 1 (Local DB)** and **Set 2 (Sync)** tasks..
```
