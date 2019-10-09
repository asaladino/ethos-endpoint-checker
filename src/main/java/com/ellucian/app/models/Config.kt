package com.ellucian.app.models

data class Config(var ethosApiKey: String? = null,
                  var onlyCheck: List<String>? = null)