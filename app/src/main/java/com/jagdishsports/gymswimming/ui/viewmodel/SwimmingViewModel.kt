package com.jagdishsports.gymswimming.ui.viewmodel

import android.app.Application
import com.jagdishsports.gymswimming.data.MemberCategories

class SwimmingViewModel(application: Application) : CategoryMembersViewModel(
    application = application,
    category = MemberCategories.SWIMMING
)
