package com.example.hostelmanagement.report.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hostelmanagement.report.infrastructure.datasource.BranchDataSource
import com.example.hostelmanagement.report.infrastructure.dto.mapper.toPieChartDataList
import com.example.hostelmanagement.report.presentation.ui.HostelReportUiState
import com.example.hostelmanagement.report.usecase.GetReportUseCase
import com.example.hostelmanagement.report.usecase.SaveReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant


class HostelReportViewModelFactory(
    private val getReportUseCase: GetReportUseCase,
    private val saveReportUseCase: SaveReportUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HostelReportViewModel::class.java)) {
            return HostelReportViewModel(getReportUseCase, saveReportUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class HostelReportViewModel (
    private val getReportUseCase: GetReportUseCase,
    private val saveReportUseCase: SaveReportUseCase,
) : ViewModel()
{

    private val _uiState = MutableStateFlow(HostelReportUiState())
    val uiState: StateFlow<HostelReportUiState> = _uiState.asStateFlow()

    private var reportJob: Job? = null



    init {
        reload()
    }


    /* ---------- UI 事件 ---------- */

    fun onTargetChanged(target: String) {
        _uiState.update { it.copy(selectedTarget = target) }
        reload()
    }

    fun onDateRangeChanged(
        startYear: Int,
        startMonth: Int,
        endYear: Int,
        endMonth: Int,
        frequency: String
    ) {
        _uiState.update {
            it.copy(
                startYear = startYear,
                startMonth = startMonth,
                endYear = endYear,
                endMonth = endMonth,
                frequency = frequency
            )
        }
        reload()
    }

    /* ---------- 数据加载 ---------- */

    fun reload() {
        reportJob?.cancel()

        reportJob = viewModelScope.launch {
            val branchId = BranchDataSource.getBranchIdByUid()
            if (branchId == null) {
                _uiState.update {
                    it.copy(loading = false, error = "Branch not found")
                }
                return@launch
            }

            val s = _uiState.value


            getReportUseCase.invoke(
                branchId = branchId,
                target = s.selectedTarget,
                frequency = s.frequency,
                startYear = s.startYear,
                startMonth = s.startMonth,
                endYear = s.endYear,
                endMonth = s.endMonth
            )
                .onStart {
                    _uiState.update { it.copy(loading = true, error = null) }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(loading = false, error = e.message)
                    }
                }
                .collect { report ->

                    if (report == null) return@collect

                    val now = Instant.now()
                    val reportEnd = report.periodEndDate

                    val isPastReport = reportEnd.isBefore(now)
                    val notUploadedYet = report.reportId.isBlank()

                    val pieData =
                        report.data.keyTimeMap.toPieChartDataList(
                            frequency = _uiState.value.frequency
                        )

                    Log.d(
                        "REPORT_VM",
                        "collect report | isPast=$isPastReport, notUploaded=$notUploadedYet"
                    )

                    // ✅ 旧的 + 还没存过 → 上传
                    if (isPastReport && notUploadedYet) {
                        Log.d("REPORT_VM", "Uploading past report to Firestore")

                        val reportToSave = report.copy(
                            reportId = "${s.selectedTarget}_${s.startYear}_${s.startMonth}_${s.endYear}_${s.endMonth}"
                        )
                        viewModelScope.launch {
                            saveReportUseCase(reportToSave)
                        }

                    } else {
                        Log.d("REPORT_VM", "Skip upload (current/future report)")
                    }

                    _uiState.update {
                        it.copy(
                            loading = false,
                            reports = report, // 如果 UI 还用 List
                            pieChartData = pieData
                        )
                    }
                }


            Log.d("REPORT_VIEWMODEL_LOG", "pieData = ${_uiState.value.pieChartData}")


        }
    }

    override fun onCleared() {
        reportJob?.cancel()
        super.onCleared()
    }


}

