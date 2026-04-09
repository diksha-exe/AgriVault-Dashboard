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
## 📁 Repository Structure

**Database:** Room (Local) & MongoDB Atlas (Cloud Sync)

```plaintext
AgriVault-Android/
├── app/
│   ├── build.gradle.kts         # Android build config (Room & WorkManager deps)
│   ├── google-services.json     # (Optional) Placeholder for Firebase
│   └── src/
│       ├── main/
│       │   ├── java/com/agrivault/app/
│       │   │   ├── data/
│       │   │   │   ├── AppDatabase.kt        # Placeholder for Room DB
│       │   │   │   ├── TransactionDao.kt     # DAO Interface
│       │   │   │   └── TransactionEntity.kt  # SQL Entity
│       │   │   ├── sync/
│       │   │   │   └── SyncWorker.kt         # Placeholder for WorkManager
│       │   │   ├── ui/
│       │   │   │   └── theme/                # Color.kt, Type.kt, Theme.kt
│       │   │   └── MainActivity.kt           # UI with intentional bugs
│       │   ├── res/
│       │   │   ├── drawable/                 # Logo and Icons
│       │   │   ├── values/                   # strings.xml (Multi-language)
│       │   │   └── xml/                      # network_security_config.xml
│       │   └── AndroidManifest.xml           # Permissions (Internet/Network State)
│       └── test/                            # Local Unit Tests
├── gradle/                                  # Gradle Wrapper files
├── .gitignore                               # Standard Android gitignore
├── build.gradle.kts                         # Project-level build file
├── gradle.properties                        # Build cache and JVM settings
├── local.properties.example                 # Template for API Keys
├── README.md                                # Competition Guide
└── settings.gradle.kts                      # Project name and repositories
```

##  Getting Started

### 1. Prerequisites
* **Android Studio Ladybug** (or later) installed.
* **JDK 17** configured in your environment.
* An Android Emulator or physical device (API 31+ recommended).

### 2. Setup Environment
Copy the example properties file and add your credentials:
```bash
cp local.properties.example local.properties


### 3. Build & Run
1. Open the project in Android Studio.
2. Wait for **Gradle** to sync all dependencies.
3. Click the **Run** icon (Green arrow) to deploy to your device.

---
##  The "Answer Key": Hidden Logical Bugs

Before implementing new features, participants must identify and fix the following logical issues in `MainActivity.kt`:

- **ID Collision:**  
  The app uses `list.size` as a Primary Key. When items are deleted, new entries may reuse existing IDs, causing duplication and data inconsistency.
  
- **Input Validation Missing:**  
  The app allows empty titles and ₹0.00 amounts. Proper validation must be implemented to ensure meaningful data entry.

- **State Leak (UI Issue):**  
  Input fields are not cleared after logging an expense, leading to accidental duplicate entries.

- **Semantic Error (Incorrect Labeling):**  
  The dashboard displays "Total Balance" but only calculates expenses. This should be corrected to "Total Spending" for accuracy.
