package com.julietkioko.mserviceai.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.julietkioko.mserviceai.data.repository.InMemoryServiceRepository
import com.julietkioko.mserviceai.domain.model.GovService
import com.julietkioko.mserviceai.domain.model.ServiceCategory
import com.julietkioko.mserviceai.domain.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the AI Assistant chat screen.
 *
 * Uses local pattern-matching against the [ServiceRepository] to answer
 * user questions about Kenyan government services. No network or API keys needed —
 * all responses are generated offline from the in-memory service catalog.
 *
 * Response strategy:
 * 1. Detect intent (greeting, list services, specific service query, category browse, help).
 * 2. Match against the service catalog using keyword scoring.
 * 3. Build a structured, conversational response with contextual follow-up suggestions.
 */
class AssistantViewModel(
    private val repository: ServiceRepository = InMemoryServiceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        // Welcome message
        val welcomeMessage = ChatMessage(
            content = "👋 Hello! I'm your MService AI assistant.\n\n" +
                "I can help you with information about Kenyan government services — " +
                "requirements, fees, step-by-step procedures, and more.\n\n" +
                "Try asking me something like:\n" +
                "• \"How do I get a KRA PIN?\"\n" +
                "• \"What do I need for a passport?\"\n" +
                "• \"Tell me about SHIF registration\"",
            isFromUser = false
        )
        _uiState.update { it.copy(messages = listOf(welcomeMessage)) }
    }

    // ── Public Actions ──────────────────────────────────────────────

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onSendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val userMessage = ChatMessage(content = text, isFromUser = true)

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isTyping = true,
                suggestedQuestions = emptyList()
            )
        }

        // Generate response (synchronous — local data only)
        val response = generateResponse(text)

        _uiState.update {
            it.copy(
                messages = it.messages + response.message,
                isTyping = false,
                suggestedQuestions = response.suggestions
            )
        }
    }

    fun onSuggestionSelected(suggestion: String) {
        _uiState.update { it.copy(inputText = suggestion) }
        onSendMessage()
    }

    // ── Response Generation Engine ──────────────────────────────────

    private data class AssistantResponse(
        val message: ChatMessage,
        val suggestions: List<String>
    )

    private fun generateResponse(query: String): AssistantResponse {
        val lower = query.lowercase().trim()

        // 1. Greetings
        if (isGreeting(lower)) {
            return AssistantResponse(
                message = ChatMessage(
                    content = "Hello! 😊 How can I help you today?\n\n" +
                        "I can assist you with any Kenyan government service — " +
                        "from KRA PIN registration to passport applications, SHIF, HELB, and more.",
                    isFromUser = false
                ),
                suggestions = listOf(
                    "What services are available?",
                    "How do I get a KRA PIN?",
                    "How do I apply for a passport?",
                    "Tell me about SHIF"
                )
            )
        }

        // 2. List all services / general help
        if (isListServicesQuery(lower)) {
            return buildServiceListResponse()
        }

        // 3. Category-specific browsing
        val categoryMatch = matchCategory(lower)
        if (categoryMatch != null) {
            return buildCategoryResponse(categoryMatch)
        }

        // 4. Specific intent: requirements
        val requirementsService = matchServiceForIntent(lower, "requirements", "documents", "what do i need", "eligibility", "what is needed", "what's needed")
        if (requirementsService != null) {
            return buildRequirementsResponse(requirementsService)
        }

        // 5. Specific intent: steps / procedure / how to
        val stepsService = matchServiceForIntent(lower, "steps", "procedure", "how do i", "how to", "process", "how can i")
        if (stepsService != null) {
            return buildStepsResponse(stepsService)
        }

        // 6. Specific intent: fees / cost
        val feesService = matchServiceForIntent(lower, "fee", "cost", "how much", "price", "charge", "pay")
        if (feesService != null) {
            return buildFeesResponse(feesService)
        }

        // 7. Specific intent: processing time / how long
        val timeService = matchServiceForIntent(lower, "how long", "processing time", "duration", "waiting", "time", "days")
        if (timeService != null) {
            return buildTimeResponse(timeService)
        }

        // 8. General service match (overview)
        val generalMatch = findBestServiceMatch(lower)
        if (generalMatch != null) {
            return buildServiceOverviewResponse(generalMatch)
        }

        // 9. Fallback
        return buildFallbackResponse()
    }

    // ── Intent Detection ────────────────────────────────────────────

    private fun isGreeting(query: String): Boolean {
        val greetings = listOf("hello", "hi", "hey", "hola", "howdy", "good morning",
            "good afternoon", "good evening", "jambo", "habari", "sasa", "niaje", "mambo")
        return greetings.any { query.startsWith(it) || query == it }
    }

    private fun isListServicesQuery(query: String): Boolean {
        val patterns = listOf(
            "what services", "list services", "all services", "available services",
            "show services", "what can you help", "what can i do", "help me",
            "what do you do", "what can you do", "services available"
        )
        return patterns.any { query.contains(it) }
    }

    private fun matchCategory(query: String): ServiceCategory? {
        val categoryKeywords = mapOf(
            ServiceCategory.TAX_REVENUE to listOf("tax", "kra", "revenue", "income tax", "itr", "pin registration"),
            ServiceCategory.HEALTH_INSURANCE to listOf("health", "shif", "sha", "nhif", "insurance", "medical", "hospital"),
            ServiceCategory.ID_PASSPORT to listOf("id card", "national id", "passport", "driving licence", "driving license", "good conduct", "police clearance", "ntsa", "immigration"),
            ServiceCategory.EDUCATION to listOf("education", "helb", "loan", "student", "university", "scholarship"),
            ServiceCategory.BUSINESS to listOf("business", "company", "registration", "permit", "licensing"),
            ServiceCategory.LAND_HOUSING to listOf("land", "title", "housing", "property", "ardhisasa")
        )

        // Only match category if the query is specifically about browsing a category
        val browsePatterns = listOf("services under", "services in", "services for", "show me", "browse", "category")
        val isCategoryBrowse = browsePatterns.any { query.contains(it) }

        if (!isCategoryBrowse) return null

        return categoryKeywords.maxByOrNull { (_, keywords) ->
            keywords.count { query.contains(it) }
        }?.takeIf { (_, keywords) ->
            keywords.any { query.contains(it) }
        }?.key
    }

    /**
     * Matches a service when the query contains BOTH a service keyword AND an intent keyword.
     */
    private fun matchServiceForIntent(query: String, vararg intentKeywords: String): GovService? {
        val hasIntent = intentKeywords.any { query.contains(it) }
        if (!hasIntent) return null
        return findBestServiceMatch(query)
    }

    /**
     * Keyword-scoring search across all services.
     * Returns the single best match, or null if no service scores above threshold.
     */
    fun findBestServiceMatch(query: String): GovService? {
        val allServices = repository.getAllServices()

        data class ScoredService(val service: GovService, val score: Int)

        val scored = allServices.map { service ->
            var score = 0

            // Title keywords (highest weight)
            service.title.split(" ", "/", "&", "(", ")").forEach { word ->
                if (word.length > 2 && query.contains(word.lowercase())) score += 10
            }

            // ID match
            if (query.contains(service.id.replace("-", " "))) score += 15

            // Agency match
            service.agency.split(" ", "(", ")").forEach { word ->
                if (word.length > 2 && query.contains(word.lowercase())) score += 5
            }

            // Summary keywords
            service.summary.split(" ").forEach { word ->
                if (word.length > 3 && query.contains(word.lowercase())) score += 2
            }

            // Common keyword aliases
            val aliases = getServiceAliases(service.id)
            aliases.forEach { alias ->
                if (query.contains(alias)) score += 12
            }

            ScoredService(service, score)
        }

        return scored
            .filter { it.score >= 10 }
            .maxByOrNull { it.score }
            ?.service
    }

    /**
     * Common shorthand keywords people use to refer to specific services.
     */
    private fun getServiceAliases(serviceId: String): List<String> {
        return when (serviceId) {
            "kra-pin-registration" -> listOf("kra pin", "pin registration", "tax pin", "kra", "itax")
            "kra-itr-filing" -> listOf("tax return", "itr", "file returns", "nil return", "tax filing", "income tax")
            "sha-shif-registration" -> listOf("shif", "sha", "nhif", "health insurance", "afya yangu", "health cover")
            "passport-application-renewal" -> listOf("passport", "e-passport", "travel document", "passport renewal")
            "national-id-card" -> listOf("national id", "id card", "maisha card", "maisha", "kitambulisho")
            "helb-loan-application" -> listOf("helb", "student loan", "university loan", "hef", "helb loan")
            "business-name-registration" -> listOf("business name", "business registration", "sole proprietorship", "register business")
            "land-title-search" -> listOf("land search", "title search", "title deed", "ardhisasa", "land ownership")
            "smart-driving-licence-renewal" -> listOf("driving licence", "driving license", "smart dl", "ntsa", "dl renewal")
            "police-clearance-good-conduct" -> listOf("good conduct", "police clearance", "clearance certificate", "dci", "certificate of good conduct")
            else -> emptyList()
        }
    }

    // ── Response Builders ───────────────────────────────────────────

    private fun buildServiceListResponse(): AssistantResponse {
        val services = repository.getAllServices()
        val grouped = services.groupBy { it.category }

        val sb = StringBuilder("Here are all the government services I can help you with:\n\n")

        grouped.forEach { (category, categoryServices) ->
            sb.append("📂 **${category.displayName}**\n")
            categoryServices.forEach { service ->
                sb.append("  • ${service.title}\n")
            }
            sb.append("\n")
        }

        sb.append("Ask me about any of these services for details on requirements, fees, steps, and processing time!")

        return AssistantResponse(
            message = ChatMessage(content = sb.toString(), isFromUser = false),
            suggestions = services.take(4).map { "Tell me about ${it.title.split(" ").take(3).joinToString(" ")}" }
        )
    }

    private fun buildCategoryResponse(category: ServiceCategory): AssistantResponse {
        val services = repository.getServicesByCategory(category)

        val sb = StringBuilder("📂 **${category.displayName}** — ${services.size} service${if (services.size != 1) "s" else ""} available:\n\n")

        services.forEach { service ->
            sb.append("• **${service.title}**\n")
            sb.append("  ${service.summary.take(80)}${if (service.summary.length > 80) "..." else ""}\n")
            sb.append("  💰 ${service.fees} · ⏱ ${service.processingTime}\n\n")
        }

        sb.append("Tap on any service name or ask me for more details!")

        return AssistantResponse(
            message = ChatMessage(content = sb.toString(), isFromUser = false),
            suggestions = services.map { "How do I apply for ${it.title.split("(").first().trim()}?" }
        )
    }

    private fun buildServiceOverviewResponse(service: GovService): AssistantResponse {
        val sb = StringBuilder("📋 **${service.title}**\n")
        sb.append("🏛 ${service.agency}\n\n")
        sb.append("${service.summary}\n\n")
        sb.append("**Key Facts:**\n")
        sb.append("💰 Fee: ${service.fees}\n")
        sb.append("⏱ Processing: ${service.processingTime}\n")
        sb.append("🌐 Portal: ${service.portalUrl}\n\n")
        sb.append("Would you like to know more about the requirements, step-by-step procedure, or anything else?")

        return AssistantResponse(
            message = ChatMessage(
                content = sb.toString(),
                isFromUser = false,
                relatedServiceId = service.id
            ),
            suggestions = listOf(
                "What are the requirements for ${service.title.split("(").first().trim()}?",
                "Show me the steps",
                "How much does it cost?",
                "What other services are available?"
            )
        )
    }

    private fun buildRequirementsResponse(service: GovService): AssistantResponse {
        val sb = StringBuilder("📝 **Requirements for ${service.title}:**\n")
        sb.append("🏛 ${service.agency}\n\n")
        service.requirements.forEachIndexed { index, req ->
            sb.append("${index + 1}. $req\n")
        }
        sb.append("\nMake sure you have all these documents ready before starting your application!")

        return AssistantResponse(
            message = ChatMessage(
                content = sb.toString(),
                isFromUser = false,
                relatedServiceId = service.id
            ),
            suggestions = listOf(
                "Show me the steps for ${service.title.split("(").first().trim()}",
                "How much does it cost?",
                "How long does it take?",
                "View full details"
            )
        )
    }

    private fun buildStepsResponse(service: GovService): AssistantResponse {
        val sb = StringBuilder("📋 **Step-by-Step: ${service.title}**\n")
        sb.append("🏛 ${service.agency}\n\n")
        service.steps.forEachIndexed { index, step ->
            sb.append("**Step ${index + 1}:** $step\n\n")
        }
        sb.append("🌐 Official portal: ${service.portalUrl}")

        return AssistantResponse(
            message = ChatMessage(
                content = sb.toString(),
                isFromUser = false,
                relatedServiceId = service.id
            ),
            suggestions = listOf(
                "What documents do I need?",
                "How much does it cost?",
                "How long does it take?",
                "View full details"
            )
        )
    }

    private fun buildFeesResponse(service: GovService): AssistantResponse {
        val content = "💰 **Fees for ${service.title}:**\n\n" +
            "${service.fees}\n\n" +
            "🏛 ${service.agency}\n" +
            "🌐 Pay via: ${service.portalUrl}\n\n" +
            "Note: All payments should be made through official government portals only."

        return AssistantResponse(
            message = ChatMessage(
                content = content,
                isFromUser = false,
                relatedServiceId = service.id
            ),
            suggestions = listOf(
                "What are the requirements?",
                "Show me the steps",
                "How long does it take?",
                "View full details"
            )
        )
    }

    private fun buildTimeResponse(service: GovService): AssistantResponse {
        val content = "⏱ **Processing Time for ${service.title}:**\n\n" +
            "${service.processingTime}\n\n" +
            "🏛 ${service.agency}\n\n" +
            "Processing times may vary depending on the volume of applications. " +
            "You can track your application status on the official portal: ${service.portalUrl}"

        return AssistantResponse(
            message = ChatMessage(
                content = content,
                isFromUser = false,
                relatedServiceId = service.id
            ),
            suggestions = listOf(
                "What are the requirements?",
                "Show me the steps",
                "How much does it cost?",
                "View full details"
            )
        )
    }

    private fun buildFallbackResponse(): AssistantResponse {
        return AssistantResponse(
            message = ChatMessage(
                content = "I'm sorry, I couldn't find a specific service matching your question. 🤔\n\n" +
                    "I can help with these Kenyan government services:\n" +
                    "• KRA PIN Registration & Tax Filing\n" +
                    "• SHIF/SHA Health Insurance\n" +
                    "• Passport Application & Renewal\n" +
                    "• National ID (Maisha Card)\n" +
                    "• HELB Student Loans\n" +
                    "• Business Name Registration\n" +
                    "• Land Title Search\n" +
                    "• Smart Driving Licence Renewal\n" +
                    "• Certificate of Good Conduct\n\n" +
                    "Try asking about a specific service, or say \"list all services\" to see everything!",
                isFromUser = false
            ),
            suggestions = listOf(
                "What services are available?",
                "How do I get a KRA PIN?",
                "Tell me about passport application",
                "How do I register for SHIF?"
            )
        )
    }

    // ── Factory ─────────────────────────────────────────────────────

    companion object {
        fun provideFactory(
            repository: ServiceRepository = InMemoryServiceRepository()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AssistantViewModel(repository) as T
            }
        }
    }
}
