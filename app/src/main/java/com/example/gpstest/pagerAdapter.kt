package com.example.gpstest

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class pagerAdapter (fm: FragmentManager): FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return OnBoarding1()
            }

            1 -> {
                return OnBoarding2()
            }

            2 -> {
                return OnBoarding3()
            }
            else -> {
                return OnBoarding1()
            }
        }
    }
}