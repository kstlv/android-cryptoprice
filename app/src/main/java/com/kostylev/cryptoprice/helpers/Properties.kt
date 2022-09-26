package com.kostylev.cryptoprice.helpers

/*
Класс, содержащий глобальные переменные

Задать значение глобальной переменной из другого класса
Properties.instance?.myValue = value

Получить значение глобальной переменной из другого класса
val value = Properties.instance?.myValue.toString()

Thanks https://stackoverflow.com/a/1944842/11038278
*/

class Properties protected constructor() {
    lateinit var currency: String

    companion object {
        private var mInstance: Properties? = null

        @get:Synchronized
        val instance: Properties?
            get() {
                if (null == mInstance) {
                    mInstance = Properties()
                }
                return mInstance
            }
    }
}