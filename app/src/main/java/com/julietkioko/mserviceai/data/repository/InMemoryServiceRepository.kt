package com.julietkioko.mserviceai.data.repository

import com.julietkioko.mserviceai.domain.model.GovService
import com.julietkioko.mserviceai.domain.model.ServiceCategory
import com.julietkioko.mserviceai.domain.repository.ServiceRepository

/**
 * In-memory repository implementation providing authoritative Kenyan public services catalog data.
 */
class InMemoryServiceRepository : ServiceRepository {

    private val services: List<GovService> = listOf(
        GovService(
            id = "kra-pin-registration",
            title = "KRA PIN Registration (Individual)",
            category = ServiceCategory.TAX_REVENUE,
            agency = "Kenya Revenue Authority (KRA)",
            summary = "Register for a Personal Identification Number (PIN) required for tax compliance, banking, and official business transactions in Kenya.",
            description = "A KRA Personal Identification Number (PIN) is an alphanumeric identifier issued by the Kenya Revenue Authority to individuals for tax administration purposes. It is mandatory for opening bank accounts, property registration, employment, and accessing government services.",
            requirements = listOf(
                "National ID card (for Kenyan citizens) or Alien Card / Valid Passport (for non-citizens)",
                "Valid personal email address (for OTP verification)",
                "Active Kenyan mobile phone number",
                "Parents' details (Names and National ID / KRA PINs if available)",
                "Residential address and Postal address"
            ),
            steps = listOf(
                "Visit the official KRA iTax portal (itax.kra.go.ke)",
                "Click on 'New PIN Registration' on the home page",
                "Select Taxpayer Type as 'Individual' and Mode of Registration as 'Online Form'",
                "Fill in Basic Information (National ID, Date of Birth, Parents' info, Address)",
                "Select applicable obligation (Income Tax Resident)",
                "Input income source details (Employment, Business, or Rental)",
                "Complete security arithmetic question and submit",
                "Download your KRA PIN Certificate and check your email for confirmation"
            ),
            fees = "Free",
            processingTime = "Instant (Upon online submission)",
            portalUrl = "https://itax.kra.go.ke",
            isPopular = true
        ),
        GovService(
            id = "kra-itr-filing",
            title = "Individual Income Tax Return Filing (ITR)",
            category = ServiceCategory.TAX_REVENUE,
            agency = "Kenya Revenue Authority (KRA)",
            summary = "File your annual individual income tax return or nil return for the preceding year of income before June 30th.",
            description = "All individuals holding an active KRA PIN are required by law to file their tax returns annually between January 1st and June 30th for the previous calendar year. Employment income earners file using the P9 form provided by employers, while individuals with no taxable income submit a Nil return.",
            requirements = listOf(
                "KRA PIN and iTax portal password",
                "P9 Certificate from employer (for employed individuals)",
                "Financial statements / books of account (for business / self-employed individuals)",
                "Withholding tax certificates (if applicable)",
                "Mortgage interest certificate / insurance relief statement (if claiming deductions)"
            ),
            steps = listOf(
                "Log in to the KRA iTax portal with your PIN and password",
                "Navigate to 'Returns' menu tab and click 'File Return' (or 'File Nil Return')",
                "Select Tax Obligation as 'Income Tax - Resident Individual'",
                "Download the Excel or ODS tax return workbook",
                "Fill in the return details (basic info, employment income from P9, allowances, and PAYE deducted)",
                "Validate the sheet to generate the zipped upload file",
                "Upload the zipped file onto the iTax portal and submit",
                "Download and keep the e-Return Acknowledgment Receipt"
            ),
            fees = "Free",
            processingTime = "Instant (Immediate acknowledgment receipt)",
            portalUrl = "https://itax.kra.go.ke",
            isPopular = true
        ),
        GovService(
            id = "sha-shif-registration",
            title = "SHIF/SHA Registration & Contribution",
            category = ServiceCategory.HEALTH_INSURANCE,
            agency = "Social Health Authority (SHA)",
            summary = "Register for the Social Health Insurance Fund (SHIF) to access universal healthcare benefits across accredited health facilities in Kenya.",
            description = "The Social Health Authority (SHA) administers the Social Health Insurance Fund (SHIF) established under the Social Health Insurance Act, transitioning from the legacy NHIF. SHIF provides Primary Healthcare, Social Health Insurance, and Emergency, Chronic and Critical Illness funds for all Kenyan residents.",
            requirements = listOf(
                "Original Kenyan National ID card or Alien Card",
                "Active Kenyan mobile phone number",
                "Dependant documentation (Spouse's National ID & Marriage Certificate; Children's Birth Certificates)",
                "Passport-size photograph (optional/digital capture)"
            ),
            steps = listOf(
                "Visit the official Afya Yangu portal (afyayangu.go.ke) or dial USSD code *147#",
                "Enter your National ID number to verify your citizen profile",
                "Verify your mobile number using the one-time password (OTP) sent via SMS",
                "Complete beneficiary demographic profile and specify household details",
                "Add dependants (spouse and children) with corresponding civil registry verification",
                "Select preferred primary healthcare facility",
                "Confirm monthly contribution calculation (2.75% of gross income for salaried or statutory minimum for informal sector)",
                "Complete registration and receive your SHA membership number via SMS"
            ),
            fees = "Free registration (Monthly contribution: 2.75% of income or KES 300 minimum for non-salaried)",
            processingTime = "Instant (Online & USSD *147#)",
            portalUrl = "https://afyayangu.go.ke",
            isPopular = true
        ),
        GovService(
            id = "passport-application-renewal",
            title = "Passport Application & Renewal",
            category = ServiceCategory.ID_PASSPORT,
            agency = "Directorate of Immigration Services",
            summary = "Apply for a new Kenyan East African e-Passport or renew an expired, filled, or mutilated passport via eCitizen.",
            description = "The Directorate of Immigration Services under the Ministry of Interior issues biometric e-Passports complying with ICAO standards. Applications and fee payments are initiated online via eCitizen, followed by an in-person biometric appointment at Immigration offices or Huduma Centres.",
            requirements = listOf(
                "eCitizen account",
                "Original National ID card and copy",
                "Original Birth Certificate and copy",
                "Previous passport and copy (for renewals)",
                "Three recent passport-size photos complying with biometric guidelines",
                "Recommender's National ID copy (e.g., advocate, religious leader, magistrate)",
                "Parents' National ID copies (or death certificates)"
            ),
            steps = listOf(
                "Log into your eCitizen account and navigate to Directorate of Immigration Services",
                "Select 'Passport Application' (Ordinary 34 pages, 50 pages, or 66 pages)",
                "Complete the online application form accurately",
                "Pay the statutory application fee via M-Pesa or card",
                "Download and print Form 19, payment invoice, and submission receipt",
                "Book a biometric capture appointment date and center",
                "Present yourself with original and physical copies of required documents for biometric capture",
                "Track processing status via SMS / eCitizen and collect your e-Passport upon notification"
            ),
            fees = "KES 7,550 (34-Page Series A) / KES 9,550 (50-Page Series B) / KES 12,550 (66-Page Series C)",
            processingTime = "14 - 30 Working Days",
            portalUrl = "https://immigration.ecitizen.go.ke",
            isPopular = true
        ),
        GovService(
            id = "national-id-card",
            title = "National ID Card Application/Replacement",
            category = ServiceCategory.ID_PASSPORT,
            agency = "National Registration Bureau (NRB)",
            summary = "Apply for a first-time National Identity Card (Maisha Card) or replace a lost, defaced, or duplicate identity card.",
            description = "The National Registration Bureau registers all Kenyan citizens aged 18 years and above, issuing the 3rd Generation National Identity Card (Maisha Card). ID cards are mandatory for all adult citizens and serve as primary legal identification in Kenya.",
            requirements = listOf(
                "Original Birth Certificate or Age Assessment Certificate",
                "Copies of parents' National ID cards (or death certificates)",
                "Two passport-size photos (for initial verification)",
                "Police Abstract and copy of lost ID (for replacement applications)",
                "Marriage Certificate (for married women changing surname)"
            ),
            steps = listOf(
                "Visit your local Chief / Sub-Chief's office for preliminary vetting (first-time applicants)",
                "Visit the nearest Huduma Centre or Sub-County Civil Registration Office (NRB)",
                "Fill in Form 1 (First-time) or Form 2 (Replacement)",
                "Present original birth certificate and parent documents for verification",
                "Undergo biometric enrollment (fingerprints, signature, and facial capture)",
                "Pay the prescribed fee (for replacements or duplicate cards)",
                "Receive the ID collection waiting slip with tracking number",
                "Collect your physical Maisha ID Card upon SMS readiness notification"
            ),
            fees = "Free for first-time applicants / KES 1,000 for replacement of lost ID / KES 2,000 for change of particulars",
            processingTime = "14 - 30 Working Days",
            portalUrl = "https://citizen.go.ke",
            isPopular = true
        ),
        GovService(
            id = "helb-loan-application",
            title = "HELB Undergraduate Loan Application",
            category = ServiceCategory.EDUCATION,
            agency = "Higher Education Loans Board (HELB)",
            summary = "Apply for Higher Education Loans Board (HELB) funding, bursaries, and scholarships under the Higher Education Financing (HEF) model.",
            description = "The Higher Education Financing portal allows direct university and TVET students placed through KUCCPS to apply for government scholarship and student loan funding under the Student-Centred Higher Education Funding Model administered by HELB and the Universities Fund.",
            requirements = listOf(
                "Valid National ID number and personal email address",
                "KCSE Index number and year of examination",
                "KUCCPS admission letter with university / college registration number",
                "Parents' / Guardian's National ID and KRA PIN numbers",
                "Two guarantors' details (ID numbers and active mobile numbers)",
                "Personal bank account registered in student's name",
                "Registered mobile number in student's name"
            ),
            steps = listOf(
                "Visit the Higher Education Financing portal (hef.co.ke)",
                "Register an account using your National ID number and verify via email/SMS OTP",
                "Log in and select 'Undergraduate / TVET Student Financial Aid Application'",
                "Complete student profile, secondary school details, and parent/guardian socio-economic background",
                "Upload required verification documents (Admission letter, ID copies)",
                "Enter bank account and guarantor details",
                "Consent to loan terms and sign application digitally via OTP",
                "Submit application and monitor Means Testing Instrument (MTI) allocation band status"
            ),
            fees = "Free",
            processingTime = "30 - 45 Working Days (Processed prior to academic semester start)",
            portalUrl = "https://www.hef.co.ke",
            isPopular = true
        ),
        GovService(
            id = "business-name-registration",
            title = "Business Name Registration",
            category = ServiceCategory.BUSINESS,
            agency = "Business Registration Service (BRS)",
            summary = "Reserve a business name and register a Sole Proprietorship or Partnership enterprise in Kenya through eCitizen.",
            description = "The Business Registration Service (BRS) administers the registration and regulation of business names under the Registration of Business Names Act. Registration protects your trade identity and enables business banking and licensing.",
            requirements = listOf(
                "eCitizen account",
                "Proposed business names (minimum 3 options in order of preference)",
                "National ID numbers and KRA PINs of proprietor(s) or partners",
                "Recent passport-sized photograph of each proprietor/partner",
                "Physical business address (Plot number, building, street, postal code, county)",
                "Clear description of primary business activities / sector"
            ),
            steps = listOf(
                "Log in to eCitizen and open 'Business Registration Service (BRS)'",
                "Click 'Make Application' and select 'Name Search and Reservation'",
                "Submit proposed names and pay name search fee (KES 150)",
                "Upon approval of reserved name, click 'Apply for Registration of Business Name'",
                "Fill in proprietor particulars, business nature, and physical location",
                "Upload proprietor ID copies, KRA PIN certificates, and photos",
                "Pay statutory registration fee (KES 950) via M-Pesa or card",
                "Download the official Certificate of Registration (BN/2)"
            ),
            fees = "KES 150 (Name Reservation) + KES 950 (Registration Fee) = KES 1,100 total",
            processingTime = "1 - 3 Working Days",
            portalUrl = "https://brs.ecitizen.go.ke",
            isPopular = true
        ),
        GovService(
            id = "land-title-search",
            title = "Land Title Search",
            category = ServiceCategory.LAND_HOUSING,
            agency = "Ministry of Lands and Physical Planning",
            summary = "Conduct an official online search on land parcel ownership, encumbrances, and acreage via the Ardhisasa portal.",
            description = "An official land search verifies the registered legal owner of a parcel, encumbrances (such as mortgages, charges, cautions, caveats, or court orders), acreage, and tenure type under the Land Registration Act through the national Ardhisasa platform.",
            requirements = listOf(
                "Active Ardhisasa personal account (verified with National ID)",
                "Title Deed number or Land Reference (L.R.) number / Block and Parcel number",
                "Scanned copy of Title Deed / Certificate of Lease (where prompted)",
                "Payment account (M-Pesa or card) for search fee"
            ),
            steps = listOf(
                "Log into the official Ardhisasa platform (ardhisasa.lands.go.ke)",
                "Under 'Services', select 'Search Property / Search Record'",
                "Enter the land parcel registration details (Registry, Block Number, Parcel Number)",
                "Review parcel location information",
                "Pay the statutory search fee (KES 500) via the integrated government payment gateway",
                "Submit search request for processing",
                "Receive search completion notification via SMS/Email",
                "Download the official digitally signed Certificate of Official Search"
            ),
            fees = "KES 500 per parcel search",
            processingTime = "Instant to 24 Hours (For digitized registries)",
            portalUrl = "https://ardhisasa.lands.go.ke",
            isPopular = false
        ),
        GovService(
            id = "smart-driving-licence-renewal",
            title = "Smart Driving Licence Renewal",
            category = ServiceCategory.ID_PASSPORT,
            agency = "National Transport and Safety Authority (NTSA)",
            summary = "Renew your valid Smart Driving Licence or apply for an interim driving licence online via the NTSA Service Portal.",
            description = "The National Transport and Safety Authority (NTSA) issues Smart Biometric Driving Licences with embedded microchips containing driver information, vehicle categories, points system, and traffic compliance history.",
            requirements = listOf(
                "eCitizen / NTSA Service Portal account",
                "Existing Driving Licence details / National ID number",
                "Active mobile phone number registered on eCitizen",
                "Medical fitness self-declaration"
            ),
            steps = listOf(
                "Log into the NTSA Service Portal via eCitizen",
                "Navigate to 'Driving Licences' section and select 'Renew Driving Licence'",
                "Choose renewal duration (1 Year or 3 Years)",
                "Review your driver particulars and residential address",
                "Pay the prescribed statutory fee via M-Pesa",
                "Instantly generate and print your Interim Driving Licence",
                "Book biometric collection center if transitioning to smart card",
                "Collect your Smart DL Card upon SMS dispatch notification"
            ),
            fees = "KES 3,050 (3-Year Smart DL Renewal) / KES 650 (1-Year Renewal)",
            processingTime = "Instant for Interim Licence / 14 - 21 Days for Smart Card Collection",
            portalUrl = "https://ntsa.ecitizen.go.ke",
            isPopular = true
        ),
        GovService(
            id = "police-clearance-good-conduct",
            title = "Certificate of Good Conduct / Police Clearance",
            category = ServiceCategory.ID_PASSPORT,
            agency = "Directorate of Criminal Investigations (DCI)",
            summary = "Apply for a Police Clearance Certificate (Certificate of Good Conduct) required for employment, visa applications, and tenders.",
            description = "The Directorate of Criminal Investigations (DCI) issues Police Clearance Certificates to Kenyan citizens and foreign residents after screening fingerprint biometric records against national criminal records in the Automated Fingerprint Identification System (AFIS).",
            requirements = listOf(
                "eCitizen account",
                "Original National ID card and clear photocopy (or Birth Certificate for minors)",
                "Two physical copies of the generated C24 fingerprint form",
                "Payment invoice receipt generated from eCitizen",
                "Physical presence for fingerprint capture at DCI Headquarters or designated Huduma Centre"
            ),
            steps = listOf(
                "Log into your eCitizen account and select 'Directorate of Criminal Investigations'",
                "Click 'Police Clearance Certificate' application",
                "Select applicant type (Adult / Child) and fingerprint capture location",
                "Pay the statutory application fee of KES 1,050 via M-Pesa",
                "Download and print the payment invoice and two copies of Form C24",
                "Visit the selected fingerprint center on the appointment date for physical biometric rolling",
                "Track processing status on your eCitizen DCI portal",
                "Download and print your digitally signed, QR-coded Police Clearance Certificate upon approval"
            ),
            fees = "KES 1,050",
            processingTime = "7 - 14 Working Days",
            portalUrl = "https://dci.ecitizen.go.ke",
            isPopular = true
        )
    )

    override fun getAllServices(): List<GovService> {
        return services
    }

    override fun getServicesByCategory(category: ServiceCategory): List<GovService> {
        return services.filter { it.category == category }
    }

    override fun searchServices(query: String): List<GovService> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return services

        return services.filter { service ->
            service.title.contains(trimmed, ignoreCase = true) ||
            service.agency.contains(trimmed, ignoreCase = true) ||
            service.summary.contains(trimmed, ignoreCase = true) ||
            service.description.contains(trimmed, ignoreCase = true) ||
            service.requirements.any { it.contains(trimmed, ignoreCase = true) }
        }
    }

    override fun getServiceById(id: String): GovService? {
        return services.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
