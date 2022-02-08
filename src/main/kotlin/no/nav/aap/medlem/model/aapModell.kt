package no.nav.aap.medlem.model

import java.time.LocalDate
import java.util.*

data class LovMe(
    val folkeregisteridentifikator: String,
    val id: UUID = UUID.randomUUID(),
    val request: MedlemRequest?,
    val response: MedlemResponse?,
)

data class MedlemRequest(
    val mottattDato: LocalDate = LocalDate.now(),
    val ytelse: String = "AAP",
    val arbeidetUtenforNorgeSiste6År: Boolean = false,
)

enum class Svar { JA, NEI, UAVKLART }

data class MedlemResponse(
    val erMedlem: Svar,
    val årsak: String?,
)