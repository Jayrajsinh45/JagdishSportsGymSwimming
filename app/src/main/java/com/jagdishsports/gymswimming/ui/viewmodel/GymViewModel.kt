package com.jagdishsports.gymswimming.ui.viewmodel

import android.app.Application
import com.jagdishsports.gymswimming.data.MemberCategories

class GymViewModel(application: Application) : CategoryMembersViewModel(
    application = application,
    category = MemberCategories.GYM
)
