package com.olx.ps.vault.utils

// 2764800 = 768h0m0s
val secretResponse = """
        {
            "lease_id": "",
            "lease_duration": 2764800,
            "renewable": false,
            "data": {
                "value1": "secret1",
                "value2": "secret2"
            }
        }
        """.trimIndent()

val initialDbResponse = """
        {
            "lease_id": "",
            "lease_duration": 5,
            "renewable": true,
            "data": {
                "username": "user",
                "password": "123"
            }
        }
        """.trimIndent()
val updatedDbResponse = """
        {
            "lease_id": "",
            "lease_duration": 5,
            "renewable": true,
            "data": {
                "username": "user",
                "password": "321"
            }
        }
        """.trimIndent()
val emptyResponse = """
        {}
    """.trimIndent()
