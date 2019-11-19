package com.razeware.emitron.utils.extensions

import retrofit2.HttpException
import retrofit2.Response
import java.net.HttpURLConnection

fun Response<*>.isBadRequest(): Boolean = this.code() == HttpURLConnection.HTTP_BAD_REQUEST

fun HttpException.isBadRequest(): Boolean = this.code() == HttpURLConnection.HTTP_BAD_REQUEST
