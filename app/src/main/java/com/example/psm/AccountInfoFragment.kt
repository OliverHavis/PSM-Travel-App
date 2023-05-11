package com.example.psm

import AccountInfoAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AccountInfoFragment(private val profileActivity: MyProfileActivity) : Fragment(), AccountInfoAdapter.OnDataChangedListener {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_info, container, false)
        recyclerView = view.findViewById(R.id.account_info_recycler_view)

        val accountInfoList = listOf(
            "Edit Name",
            "Edit Email",
            "Edit Phone Number",
            "Edit Address",
            "Change Password"

        )

        val adapter = AccountInfoAdapter(accountInfoList, requireContext())
        adapter.setOnDataChangedListener(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onDataChanged() {
        profileActivity.setupUI()
    }
}