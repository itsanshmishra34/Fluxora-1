package com.example.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    SPANISH("es", "Spanish", "Español"),
    FRENCH("fr", "French", "Français");

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

val LocalLanguageCode = staticCompositionLocalOf { "en" }

@Composable
fun t(key: String): String {
    val lang = LocalLanguageCode.current
    return LanguageManager.getString(key, lang)
}

object LanguageManager {

    private val translations: Map<String, Map<String, String>> = mapOf(
        "en" to mapOf(
            // Navigation & Main Tabs
            "dashboard" to "Dashboard",
            "tasks" to "Tasks",
            "projects" to "Projects",
            "calendar" to "Calendar",
            "analytics" to "Analytics",
            "ledger" to "Transactions",
            "planning" to "Budgets",
            "roommates" to "Roommates",
            "settings" to "Settings",
            "more" to "More",

            // Auth Screen
            "welcome_back" to "Welcome Back",
            "sign_in" to "Sign In",
            "sign_in_subtitle" to "Sign in to continue your journey",
            "email_address" to "Email address",
            "password" to "Password",
            "forgot_password" to "Forgot Password?",
            "continue_with_google" to "Continue with Google",
            "demo_login" to "Demo Login",
            "or" to "OR",
            "authenticating_google" to "Authenticating with Google...",

            // Dashboard
            "good_morning" to "Good Morning, Ayush 👋",
            "stay_focused" to "Stay focused, track progress and grow consistently.",
            "your_productivity_flow" to "Your Productivity Flow",
            "keep_the_momentum" to "Keep the momentum. You're 68% ahead of last week.",
            "tasks_today" to "Tasks Today",
            "focus_time" to "Focus Time",
            "goals_progress" to "Goals Progress",
            "streak" to "Streak",
            "days" to "days",
            "todays_tasks" to "Today's Tasks",
            "weekly_focus" to "Weekly Focus",
            "overall_progress" to "Overall Progress",
            "from_last_week" to "from last week",
            "search_placeholder" to "Search anything... (Ctrl K)",
            "net_spend" to "Net Spend",
            "safe_to_spend" to "Safe to Spend Today",
            "monthly_cash_flow" to "Monthly Cash Flow",
            "quick_add" to "Quick Add",
            "start_focus" to "Start Focus",
            "daily_insight" to "Daily AI Saving Insight",

            // Tasks, Projects, Calendar & Analytics
            "all" to "All",
            "active" to "Active",
            "completed" to "Completed",
            "in_progress" to "In Progress",
            "add_task" to "Add Task",
            "new_task" to "New Task",
            "task_description" to "Task description...",
            "add_project" to "Add Project",
            "events" to "Events",
            "tasks_completed" to "Tasks Completed",
            "productivity" to "Productivity",
            "this_month" to "This Month",
            "tag_study" to "Study",
            "tag_build" to "Build",
            "tag_docs" to "Docs",
            "tag_health" to "Health",
            "tag_dsa" to "DSA",

            // Finance / Ledger / Budgets / Roommates
            "add_transaction" to "Add Transaction",
            "title" to "Title",
            "amount" to "Amount",
            "category" to "Category",
            "income" to "Income",
            "expense" to "Expense",
            "notes" to "Notes",
            "export_csv" to "Export CSV",
            "import_csv" to "Import CSV",
            "savings_goals" to "Savings Goals",
            "recurring_bills" to "Recurring Bills",
            "add_goal" to "Add Goal",
            "add_bill" to "Add Bill",
            "pay_bill" to "Pay Bill",
            "roommate_split" to "Roommate Split",
            "household_members" to "Household Members",
            "shared_expenses" to "Shared Expenses",
            "settle_up" to "Settle Up",

            // Budget Goals
            "monthly_category_budgets" to "Budget Goals & Category Limits",
            "set_budget_goal" to "Set Budget Goal",
            "remaining" to "remaining",
            "over_limit" to "over limit",
            "on_track" to "On Track",
            "near_limit" to "Near Limit",
            "exceeded_limit" to "Exceeded Limit",
            "select_category" to "Select Category",
            "monthly_limit_inr" to "Monthly Limit in INR",
            "save_budget" to "Save Budget Goal",

            // Debt Calculator
            "debt_calculator_title" to "Debt Payoff Calculator",
            "debt_payoff_strategy" to "Debt Payoff Strategy",
            "snowball_strategy" to "Debt Snowball ⚡",
            "avalanche_strategy" to "Debt Avalanche 🏔️",
            "snowball_desc" to "Pay lowest balance first for fast momentum and quick wins.",
            "avalanche_desc" to "Pay highest interest rate (APR) first to save the most money.",
            "add_debt" to "Add Debt",
            "edit_debt" to "Edit Debt",
            "debt_name" to "Debt Name (e.g. Credit Card, Loan)",
            "balance_inr" to "Current Balance in INR",
            "interest_rate" to "Interest Rate (% APR)",
            "min_payment_inr" to "Minimum Monthly Payment in INR",
            "extra_monthly_payment" to "Extra Monthly Payment",
            "total_debt" to "Total Outstanding Debt",
            "time_to_debt_free" to "Time to Debt-Free",
            "total_interest" to "Total Interest Paid",
            "months" to "months",
            "save_debt" to "Save Debt",
            "payoff_sequence" to "Payoff Sequence & Schedule",
            "avalanche_savings_notice" to "Debt Avalanche saves you %s in interest compared to Snowball!",
            "snowball_momentum_notice" to "Debt Snowball pays off your first debt in %d months for quick momentum!",
            "extra_payment_impact" to "Adding %s extra/month makes you debt-free %d months faster!",
            "no_debts" to "No debts added. Add your credit cards or loans to calculate your debt-free schedule.",

            // Onboarding
            "onboarding_title_1" to "Welcome to Fluxora",
            "onboarding_desc_1" to "Your household money, simple and private. Let's get you set up.",
            "onboarding_title_2" to "Stay Informed",
            "onboarding_desc_2" to "Enable notifications to get timely budget alerts, bill reminders, and daily financial insights.",
            "onboarding_title_3" to "Unlock Features",
            "onboarding_desc_3" to "Experience the Cash-Flow Reactor, automated ledger, and our secure roommate split system.",
            "get_started" to "Get Started",

            // Settings & Governance
            "settings_governance" to "Settings & Governance",
            "profile" to "Profile",
            "appearance" to "Appearance",
            "theme_mode" to "Theme Mode",
            "dark_mode" to "Dark Mode",
            "accent_color" to "Accent Color",
            "change_accent" to "Change Accent",
            "preferences" to "Preferences",
            "app_language" to "App Language",
            "notifications" to "Notifications",
            "mask_sensitive_amounts" to "Mask Sensitive Amounts",
            "biometric_lock" to "Biometric App Lock",
            "advanced_settings" to "Advanced Settings",
            "ai_advisor" to "AI Advisor",
            "delete_all_data" to "Delete All Account Data",
            "sign_out" to "Sign Out",
            "cinematic_design_system" to "Cinematic Design System",

            // Common Actions & Dialogs
            "save" to "Save",
            "cancel" to "Cancel",
            "close" to "Close",
            "delete" to "Delete",
            "confirm" to "Confirm",
            "back" to "Back"
        ),
        "hi" to mapOf(
            // Navigation & Main Tabs
            "dashboard" to "डैशबोर्ड",
            "tasks" to "कार्य (Tasks)",
            "projects" to "परियोजनाएं",
            "calendar" to "कैलेंडर",
            "analytics" to "विश्लेषण",
            "ledger" to "लेन-देन",
            "planning" to "बजट",
            "roommates" to "रूममेट्स",
            "settings" to "सेटिंग्स",
            "more" to "अधिक",

            // Auth Screen
            "welcome_back" to "वापसी पर स्वागत है",
            "sign_in" to "साइन इन करें",
            "sign_in_subtitle" to "अपनी यात्रा जारी रखने के लिए साइन इन करें",
            "email_address" to "ईमेल पता",
            "password" to "पासवर्ड",
            "forgot_password" to "पासवर्ड भूल गए?",
            "continue_with_google" to "गूगल के साथ जारी रखें",
            "demo_login" to "डेमो लॉगिन",
            "or" to "या",
            "authenticating_google" to "गूगल के साथ प्रमाणीकृत हो रहा है...",

            // Dashboard
            "good_morning" to "सुप्रभात, आयुष 👋",
            "stay_focused" to "ध्यान केंद्रित रखें, प्रगति ट्रैक करें और लगातार बढ़ें।",
            "your_productivity_flow" to "आपका उत्पादकता प्रवाह",
            "keep_the_momentum" to "गति बनाए रखें। आप पिछले सप्ताह की तुलना में 68% आगे हैं।",
            "tasks_today" to "आज के कार्य",
            "focus_time" to "फ़ोकस समय",
            "goals_progress" to "लक्ष्य प्रगति",
            "streak" to "लगातार दिन",
            "days" to "दिन",
            "todays_tasks" to "आज के कार्य",
            "weekly_focus" to "साप्ताहिक फ़ोकस",
            "overall_progress" to "कुल प्रगति",
            "from_last_week" to "पिछले सप्ताह की तुलना में",
            "search_placeholder" to "कुछ भी खोजें... (Ctrl K)",
            "net_spend" to "शुद्ध खर्च",
            "safe_to_spend" to "आज खर्च करने के लिए सुरक्षित",
            "monthly_cash_flow" to "मासिक नकद प्रवाह",
            "quick_add" to "त्वरित जोड़ें",
            "start_focus" to "फ़ोकस शुरू करें",
            "daily_insight" to "दैनिक एआई बचत सुझाव",

            // Tasks, Projects, Calendar & Analytics
            "all" to "सभी",
            "active" to "सक्रिय",
            "completed" to "पूर्ण",
            "in_progress" to "प्रगति पर",
            "add_task" to "कार्य जोड़ें",
            "new_task" to "नया कार्य",
            "task_description" to "कार्य विवरण...",
            "add_project" to "परियोजना जोड़ें",
            "events" to "कार्यक्रम",
            "tasks_completed" to "पूर्ण कार्य",
            "productivity" to "उत्पादकता",
            "this_month" to "इस महीने",
            "tag_study" to "अध्ययन",
            "tag_build" to "निर्माण",
            "tag_docs" to "दस्तावेज़",
            "tag_health" to "स्वास्थ्य",
            "tag_dsa" to "डीएसए",

            // Finance / Ledger / Budgets / Roommates
            "add_transaction" to "लेन-देन जोड़ें",
            "title" to "शीर्षक",
            "amount" to "राशि",
            "category" to "श्रेणी",
            "income" to "आय",
            "expense" to "खर्च",
            "notes" to "नोट्स",
            "export_csv" to "CSV निर्यात करें",
            "import_csv" to "CSV आयात करें",
            "savings_goals" to "बचत लक्ष्य",
            "recurring_bills" to "आवर्ती बिल",
            "add_goal" to "लक्ष्य जोड़ें",
            "add_bill" to "बिल जोड़ें",
            "pay_bill" to "बिल भुगतान करें",
            "roommate_split" to "रूममेट बंटवारा",
            "household_members" to "घर के सदस्य",
            "shared_expenses" to "साझा खर्च",
            "settle_up" to "हिसाब बराबर करें",

            // Budget Goals
            "monthly_category_budgets" to "बजट लक्ष्य और श्रेणी सीमाएं",
            "set_budget_goal" to "बजट लक्ष्य सेट करें",
            "remaining" to "शेष",
            "over_limit" to "सीमा से अधिक",
            "on_track" to "सही राह पर",
            "near_limit" to "सीमा के पास",
            "exceeded_limit" to "सीमा पार हो गई",
            "select_category" to "श्रेणी चुनें",
            "monthly_limit_inr" to "मासिक सीमा (रुपये में)",
            "save_budget" to "बजट लक्ष्य सहेजें",

            // Debt Calculator
            "debt_calculator_title" to "ऋण भुगतान कैलकुलेटर (Debt Calculator)",
            "debt_payoff_strategy" to "ऋण भुगतान रणनीति",
            "snowball_strategy" to "डेट स्नोबॉल ⚡",
            "avalanche_strategy" to "डेट एवलांच 🏔️",
            "snowball_desc" to "तेज़ प्रगति के लिए सबसे कम शेष राशि का पहले भुगतान करें।",
            "avalanche_desc" to "अधिकतम बचत के लिए उच्चतम ब्याज दर (APR) का पहले भुगतान करें।",
            "add_debt" to "ऋण जोड़ें",
            "edit_debt" to "ऋण संपादित करें",
            "debt_name" to "ऋण का नाम (जैसे क्रेडिट कार्ड, ऋण)",
            "balance_inr" to "वर्तमान शेष राशि (INR)",
            "interest_rate" to "ब्याज दर (% APR)",
            "min_payment_inr" to "न्यूनतम मासिक भुगतान (INR)",
            "extra_monthly_payment" to "अतिरिक्त मासिक भुगतान",
            "total_debt" to "कुल बकाया ऋण",
            "time_to_debt_free" to "ऋण-मुक्त होने का समय",
            "total_interest" to "कुल भुगतान किया गया ब्याज",
            "months" to "महीने",
            "save_debt" to "ऋण सहेजें",
            "payoff_sequence" to "भुगतान क्रम और समय सारणी",
            "avalanche_savings_notice" to "स्नोबॉल की तुलना में एवलांच रणनीति आपको %s ब्याज बचाती है!",
            "snowball_momentum_notice" to "स्नोबॉल रणनीति आपके पहले ऋण का %d महीने में भुगतान कर देती है!",
            "extra_payment_impact" to "प्रति माह %s अतिरिक्त भुगतान आपको %d महीने पहले ऋण-मुक्त करता है!",
            "no_debts" to "कोई ऋण नहीं जोड़ा गया। अपनी ऋण-मुक्त समय सारणी की गणना के लिए अपने क्रेडिट कार्ड या ऋण जोड़ें।",

            // Settings & Governance
            "settings_governance" to "सेटिंग्स और शासन",
            "profile" to "प्रोफ़ाइल",
            "appearance" to "रंग-रूप",
            "theme_mode" to "थीम मोड",
            "dark_mode" to "डार्क मोड",
            "accent_color" to "मुख्य रंग",
            "change_accent" to "रंग बदलें",
            "preferences" to "प्राथमिकताएं",
            "app_language" to "ऐप भाषा",
            "notifications" to "सूचनाएं",
            "mask_sensitive_amounts" to "संवेदनशील राशि छिपाएं",
            "biometric_lock" to "बायोमेट्रिक ऐप लॉक",
            "advanced_settings" to "उन्नत सेटिंग्स",
            "ai_advisor" to "एआई सलाहकार",
            "delete_all_data" to "सभी खाता डेटा हटाएँ",
            "sign_out" to "साइन आउट",
            "cinematic_design_system" to "सिनेमैटिक डिज़ाइन सिस्टम",

            // Common Actions & Dialogs
            "save" to "सहेजें",
            "cancel" to "रद्द करें",
            "close" to "बंद करें",
            "delete" to "हटाएं",
            "confirm" to "पुष्टि करें",
            "back" to "वापस"
        ),
        "es" to mapOf(
            // Navigation & Main Tabs
            "dashboard" to "Panel",
            "tasks" to "Tareas",
            "projects" to "Proyectos",
            "calendar" to "Calendario",
            "analytics" to "Analítica",
            "ledger" to "Transacciones",
            "planning" to "Presupuestos",
            "roommates" to "Compañeros",
            "settings" to "Ajustes",
            "more" to "Más",

            // Auth Screen
            "welcome_back" to "Bienvenido de nuevo",
            "sign_in" to "Iniciar Sesión",
            "sign_in_subtitle" to "Inicia sesión para continuar tu viaje",
            "email_address" to "Correo electrónico",
            "password" to "Contraseña",
            "forgot_password" to "¿Olvidaste tu contraseña?",
            "continue_with_google" to "Continuar con Google",
            "demo_login" to "Acceso de Prueba",
            "or" to "O",
            "authenticating_google" to "Autenticando con Google...",

            // Dashboard
            "good_morning" to "Buenos Días, Ayush 👋",
            "stay_focused" to "Mantente enfocado, rastrea el progreso y crece constantemente.",
            "your_productivity_flow" to "Tu Flujo de Productividad",
            "keep_the_momentum" to "Mantén el impulso. Estás 68% por delante de la semana pasada.",
            "tasks_today" to "Tareas de Hoy",
            "focus_time" to "Tiempo de Enfoque",
            "goals_progress" to "Progreso de Objetivos",
            "streak" to "Racha",
            "days" to "días",
            "todays_tasks" to "Tareas de Hoy",
            "weekly_focus" to "Enfoque Semanal",
            "overall_progress" to "Progreso General",
            "from_last_week" to "respecto a la semana pasada",
            "search_placeholder" to "Buscar algo... (Ctrl K)",
            "net_spend" to "Gasto Neto",
            "safe_to_spend" to "Seguro para Gastar Hoy",
            "monthly_cash_flow" to "Flujo de Caja Mensual",
            "quick_add" to "Añadir Rápido",
            "start_focus" to "Iniciar Enfoque",
            "daily_insight" to "Sugerencia Diaria de Ahorro con IA",

            // Tasks, Projects, Calendar & Analytics
            "all" to "Todas",
            "active" to "Activas",
            "completed" to "Completadas",
            "in_progress" to "En Proceso",
            "add_task" to "Añadir Tarea",
            "new_task" to "Nueva Tarea",
            "task_description" to "Descripción de la tarea...",
            "add_project" to "Añadir Proyecto",
            "events" to "Eventos",
            "tasks_completed" to "Tareas Completadas",
            "productivity" to "Productividad",
            "this_month" to "Este Mes",
            "tag_study" to "Estudio",
            "tag_build" to "Construir",
            "tag_docs" to "Docs",
            "tag_health" to "Salud",
            "tag_dsa" to "DSA",

            // Finance / Ledger / Budgets / Roommates
            "add_transaction" to "Añadir Transacción",
            "title" to "Título",
            "amount" to "Monto",
            "category" to "Categoría",
            "income" to "Ingresos",
            "expense" to "Gastos",
            "notes" to "Notas",
            "export_csv" to "Exportar CSV",
            "import_csv" to "Importar CSV",
            "savings_goals" to "Metas de Ahorro",
            "recurring_bills" to "Facturas Recurrentes",
            "add_goal" to "Añadir Meta",
            "add_bill" to "Añadir Factura",
            "pay_bill" to "Pagar Factura",
            "roommate_split" to "División entre Compañeros",
            "household_members" to "Miembros del Hogar",
            "shared_expenses" to "Gastos Compartidos",
            "settle_up" to "Saldar Cuentas",

            // Budget Goals
            "monthly_category_budgets" to "Metas de Presupuesto y Límites",
            "set_budget_goal" to "Establecer Meta de Presupuesto",
            "remaining" to "restante",
            "over_limit" to "sobre el límite",
            "on_track" to "En Camino",
            "near_limit" to "Cerca del Límite",
            "exceeded_limit" to "Límite Excedido",
            "select_category" to "Seleccionar Categoría",
            "monthly_limit_inr" to "Límite Mensual en INR",
            "save_budget" to "Guardar Meta de Presupuesto",

            // Debt Calculator
            "debt_calculator_title" to "Calculadora de Pago de Deudas",
            "debt_payoff_strategy" to "Estrategia de Pago de Deudas",
            "snowball_strategy" to "Bola de Nieve ⚡",
            "avalanche_strategy" to "Avalancha de Deudas 🏔️",
            "snowball_desc" to "Paga primero el saldo más bajo para un impulso rápido.",
            "avalanche_desc" to "Paga primero la tasa de interés más alta (APR) para ahorrar más.",
            "add_debt" to "Añadir Deuda",
            "edit_debt" to "Editar Deuda",
            "debt_name" to "Nombre de la Deuda (ej. Tarjeta de Crédito)",
            "balance_inr" to "Saldo Actual en INR",
            "interest_rate" to "Tasa de Interés (% APR)",
            "min_payment_inr" to "Pago Mínimo Mensual en INR",
            "extra_monthly_payment" to "Pago Mensual Adicional",
            "total_debt" to "Deuda Total Pendiente",
            "time_to_debt_free" to "Tiempo para Liberarse de Deudas",
            "total_interest" to "Interés Total Pagado",
            "months" to "meses",
            "save_debt" to "Guardar Deuda",
            "payoff_sequence" to "Secuencia y Cronograma de Pago",
            "avalanche_savings_notice" to "¡La estrategia Avalancha te ahorra %s en intereses frente a Bola de Nieve!",
            "snowball_momentum_notice" to "¡La Bola de Nieve liquida tu primera deuda en %d meses para un impulso rápido!",
            "extra_payment_impact" to "¡Añadir %s extra/mes te libera de deudas %d meses antes!",
            "no_debts" to "No hay deudas añadidas. Añade tus tarjetas de crédito o préstamos para simular tu plan.",

            // Settings & Governance
            "settings_governance" to "Ajustes y Gobernanza",
            "profile" to "Perfil",
            "appearance" to "Apariencia",
            "theme_mode" to "Modo de Tema",
            "dark_mode" to "Modo Oscuro",
            "accent_color" to "Color de Acento",
            "change_accent" to "Cambiar Acento",
            "preferences" to "Preferencias",
            "app_language" to "Idioma de la App",
            "notifications" to "Notificaciones",
            "mask_sensitive_amounts" to "Ocultar Montos Sensibles",
            "biometric_lock" to "Bloqueo Biométrico",
            "advanced_settings" to "Ajustes Avanzados",
            "ai_advisor" to "Asesor de IA",
            "delete_all_data" to "Eliminar Todos los Datos",
            "sign_out" to "Cerrar Sesión",
            "cinematic_design_system" to "Sistema de Diseño Cinematográfico",

            // Common Actions & Dialogs
            "save" to "Guardar",
            "cancel" to "Cancelar",
            "close" to "Cerrar",
            "delete" to "Eliminar",
            "confirm" to "Confirmar",
            "back" to "Atrás"
        ),
        "fr" to mapOf(
            // Navigation & Main Tabs
            "dashboard" to "Tableau de Bord",
            "tasks" to "Tâches",
            "projects" to "Projets",
            "calendar" to "Calendrier",
            "analytics" to "Analytique",
            "ledger" to "Transactions",
            "planning" to "Budgets",
            "roommates" to "Colocataires",
            "settings" to "Paramètres",
            "more" to "Plus",

            // Auth Screen
            "welcome_back" to "Bon retour",
            "sign_in" to "Se Connecter",
            "sign_in_subtitle" to "Connectez-vous pour continuer votre parcours",
            "email_address" to "Adresse e-mail",
            "password" to "Mot de passe",
            "forgot_password" to "Mot de passe oublié ?",
            "continue_with_google" to "Continuer avec Google",
            "demo_login" to "Connexion Démo",
            "or" to "OU",
            "authenticating_google" to "Authentification avec Google...",

            // Dashboard
            "good_morning" to "Bonjour, Ayush 👋",
            "stay_focused" to "Restez concentré, suivez vos progrès et grandissez constamment.",
            "your_productivity_flow" to "Votre Flux de Productivité",
            "keep_the_momentum" to "Gardez l'élan. Vous avez 68% d'avance sur la semaine dernière.",
            "tasks_today" to "Tâches d'Aujourd'hui",
            "focus_time" to "Temps de Concentration",
            "goals_progress" to "Progrès des Objectifs",
            "streak" to "Série",
            "days" to "jours",
            "todays_tasks" to "Tâches du Jour",
            "weekly_focus" to "Focus Hebdomadaire",
            "overall_progress" to "Progrès Global",
            "from_last_week" to "par rapport à la semaine dernière",
            "search_placeholder" to "Rechercher... (Ctrl K)",
            "net_spend" to "Dépense Nette",
            "safe_to_spend" to "Sécurisé à Dépenser Aujourd'hui",
            "monthly_cash_flow" to "Flux de Trésorerie Mensuel",
            "quick_add" to "Ajout Rapide",
            "start_focus" to "Démarrer Focus",
            "daily_insight" to "Conseil Quotidien d'Épargne IA",

            // Tasks, Projects, Calendar & Analytics
            "all" to "Tous",
            "active" to "Actifs",
            "completed" to "Terminés",
            "in_progress" to "En Cours",
            "add_task" to "Ajouter une Tâche",
            "new_task" to "Nouvelle Tâche",
            "task_description" to "Description de la tâche...",
            "add_project" to "Ajouter un Projet",
            "events" to "Événements",
            "tasks_completed" to "Tâches Terminées",
            "productivity" to "Productivité",
            "this_month" to "Ce Mois",
            "tag_study" to "Étude",
            "tag_build" to "Créer",
            "tag_docs" to "Docs",
            "tag_health" to "Santé",
            "tag_dsa" to "DSA",

            // Finance / Ledger / Budgets / Roommates
            "add_transaction" to "Ajouter une Transaction",
            "title" to "Titre",
            "amount" to "Montant",
            "category" to "Catégorie",
            "income" to "Revenus",
            "expense" to "Dépenses",
            "notes" to "Notes",
            "export_csv" to "Exporter CSV",
            "import_csv" to "Importer CSV",
            "savings_goals" to "Objectifs d'Épargne",
            "recurring_bills" to "Factures Récurrentes",
            "add_goal" to "Ajouter un Objectif",
            "add_bill" to "Ajouter une Facture",
            "pay_bill" to "Payer la Facture",
            "roommate_split" to "Partage entre Colocataires",
            "household_members" to "Membres du Foyer",
            "shared_expenses" to "Dépenses Partagées",
            "settle_up" to "Régler les Comptes",

            // Budget Goals
            "monthly_category_budgets" to "Objectifs de Budget et Limites",
            "set_budget_goal" to "Définir l'Objectif de Budget",
            "remaining" to "restant",
            "over_limit" to "au-dessus de la limite",
            "on_track" to "Sur la Bonne Voie",
            "near_limit" to "Proche de la Limite",
            "exceeded_limit" to "Limite Dépassée",
            "select_category" to "Sélectionner la Catégorie",
            "monthly_limit_inr" to "Limite Mensuelle en INR",
            "save_budget" to "Enregistrer l'Objectif de Budget",

            // Debt Calculator
            "debt_calculator_title" to "Calculateur de Remboursement de Dette",
            "debt_payoff_strategy" to "Stratégie de Remboursement",
            "snowball_strategy" to "Boule de Neige ⚡",
            "avalanche_strategy" to "Avalanche de Dette 🏔️",
            "snowball_desc" to "Remboursez d'abord le plus petit solde pour un élan rapide.",
            "avalanche_desc" to "Remboursez d'abord le taux d'intérêt le plus élevé (APR) pour économiser plus.",
            "add_debt" to "Ajouter une Dette",
            "edit_debt" to "Modifier la Dette",
            "debt_name" to "Nom de la Dette (ex. Carte de Crédit)",
            "balance_inr" to "Solde Actuel en INR",
            "interest_rate" to "Taux d'intérêt (% APR)",
            "min_payment_inr" to "Paiement Mensuel Minimum en INR",
            "extra_monthly_payment" to "Paiement Mensuel Supplémentaire",
            "total_debt" to "Dette Totale Restante",
            "time_to_debt_free" to "Délai avant Libération des Dettes",
            "total_interest" to "Intérêt Total Payé",
            "months" to "mois",
            "save_debt" to "Enregistrer la Dette",
            "payoff_sequence" to "Séquence et Calendrier de Remboursement",
            "avalanche_savings_notice" to "La stratégie Avalanche vous fait économiser %s en intérêts par rapport à la Boule de Neige !",
            "snowball_momentum_notice" to "La Boule de Neige rembourse votre première dette en %d mois pour un élan rapide !",
            "extra_payment_impact" to "Ajouter %s de plus/mois vous libère des dettes %d mois plus tôt !",
            "no_debts" to "Aucune dette ajoutée. Ajoutez vos cartes de crédit ou prêts pour simular votre calendrier.",

            // Settings & Governance
            "settings_governance" to "Paramètres et Gouvernance",
            "profile" to "Profil",
            "appearance" to "Apparence",
            "theme_mode" to "Mode Thème",
            "dark_mode" to "Mode Sombre",
            "accent_color" to "Couleur d'Accent",
            "change_accent" to "Changer l'Accent",
            "preferences" to "Préférences",
            "app_language" to "Langue de l'App",
            "notifications" to "Notifications",
            "mask_sensitive_amounts" to "Masquer les Montants Sensibles",
            "biometric_lock" to "Verrouillage Biométrique",
            "advanced_settings" to "Paramètres Avancés",
            "ai_advisor" to "Conseiller IA",
            "delete_all_data" to "Supprimer Toutes les Données",
            "sign_out" to "Déconnexion",
            "cinematic_design_system" to "Système de Design Cinématique",

            // Common Actions & Dialogs
            "save" to "Enregistrer",
            "cancel" to "Annuler",
            "close" to "Fermer",
            "delete" to "Supprimer",
            "confirm" to "Confirmer",
            "back" to "Retour"
        )
    )

    fun getString(key: String, languageCode: String): String {
        val langMap = translations[languageCode.lowercase()] ?: translations["en"]!!
        return langMap[key] ?: translations["en"]?.get(key) ?: key
    }

    fun formatDate(timestampMillis: Long, languageCode: String): String {
        val locale = getLocaleForCode(languageCode)
        val formatter = SimpleDateFormat("MMM dd, yyyy", locale)
        return formatter.format(Date(timestampMillis))
    }

    fun formatNumber(number: Number, languageCode: String): String {
        val locale = getLocaleForCode(languageCode)
        return NumberFormat.getInstance(locale).format(number)
    }

    fun formatCurrency(amountRupees: Double, languageCode: String, symbol: String = "₹"): String {
        val locale = getLocaleForCode(languageCode)
        val formattedNumber = NumberFormat.getInstance(locale).format(amountRupees)
        return "$symbol$formattedNumber"
    }

    private fun getLocaleForCode(languageCode: String): Locale {
        return when (languageCode.lowercase()) {
            "hi" -> Locale("hi", "IN")
            "es" -> Locale("es", "ES")
            "fr" -> Locale("fr", "FR")
            else -> Locale.ENGLISH
        }
    }
}
