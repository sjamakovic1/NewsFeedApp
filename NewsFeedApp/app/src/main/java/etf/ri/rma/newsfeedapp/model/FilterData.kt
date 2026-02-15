package etf.ri.rma.newsfeedapp.model

import java.io.Serializable

data class FilterData(
    val kategorija: String,
    val startDate:  String?,
    val endDate: String?,
    val unwantedWords: List<String>
) : Serializable