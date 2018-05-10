package com.olx.ps.vault.common.test

// 2764800 = 768h0m0s
const val SECRET_RESPONSE = """
{
        "lease_id": "",
        "lease_duration": 2764800,
        "renewable": false,
        "data": {
            "value1": "secret1",
            "value2": "secret2"
    }
}
"""

const val INITIAL_DB_RESPONSE = """
{
        "lease_id": "",
        "lease_duration": 5,
        "renewable": true,
        "data": {
            "username": "user",
            "password": "123"
    }
}
"""

const val UPDATED_DB_RESPONSE = """
{
        "lease_id": "",
        "lease_duration": 5,
        "renewable": true,
        "data": {
            "username": "user",
            "password": "321"
    }
}
"""

const val EMPTY_RESPONSE = "{}"
