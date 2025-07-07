package com.cellblock70.popularmovies.domain.usecase

import android.content.Context
import com.cellblock70.popularmovies.R
import com.cellblock70.popularmovies.data.TabPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetSelectedTabUseCase @Inject constructor(@ApplicationContext private val context: Context) {

    suspend operator fun invoke(): String {
        return TabPreferences.getSelectedTab(context).first() ?: context.getString(R.string.popular)
    }
}