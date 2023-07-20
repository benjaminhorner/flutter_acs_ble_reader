package com.benjamin.horner.flutter_acs_card_reader

interface OnUpdateConnectionState {
    fun updateState(state: Int,isConnected: Boolean, newState: Int)
}