import java.util.concurrent.ConcurrentHashMap
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.ScheduledThreadPoolExecutor


// Класс клиент
class Client(
    val id: Int,
    var balance: Double,
    var currency: String,
    ){
    val lock = ReentrantLock()
    override fun toString(): String = "id: $id  balance: $balance currency $currency "
}

// SOLID
class Logger: Observer{
    override fun update(message: String){
        println("Сообщение: $message")
    }
}


interface Observer{
    fun update(message: String)
}

// Описание класса касса
class Cashier(val clientId: Int, val bank: Bank) : Thread() {

    val exchangeRates = ConcurrentHashMap<String, Double>()

    // Положить на депозит
    fun deposit(clientId: Int, amount: Double) {
        bank.clients[clientId]?.let {
            synchronized(it.lock) {
                it.balance += amount
            }
        }
    }



    // Снять деньги со счета
    fun withdraw(clientId: Int, amount: Double) {
        bank.clients[clientId]?.let {
            synchronized(it.lock) {
                it.balance -= amount
            }
        }
    }

    // Обмен валюты
    fun exchangeCurrency(clientId: Int, fromCurrency: String, toCurrency: String) {

        bank.clients[clientId]?.let{
            synchronized(it.lock) {
                val rate = exchangeRates[toCurrency]!! / exchangeRates[fromCurrency]!!
                val newBalance = it.balance * rate

                it.balance = newBalance
                it.currency = toCurrency
            }
        }
    }



    // Перевод валюты
    fun transferFunds(senderId: Int, receiverId: Int, amount: Double)  {
        bank.clients[clientId]?.let {
            synchronized(it.lock) {
                bank.clients[senderId]?.let {
                    it.balance-=amount
                }
                bank.clients[receiverId]?.let {
                    it.balance+=amount
                }
            }
            }
        }
    // Генератор курса валют
    fun ExchangeRateUpdate(): Double{
        val add = (-1000..1000).random()
        return add / 1.0
    }

    init {
        // Инициализируем курсы валют
        exchangeRates["USD"] = 1.0
        exchangeRates["EUR"] = 0.9 * exchangeRates["USD"]!!
        exchangeRates["RUB"] = 30 * exchangeRates["USD"]!!
        exchangeRates["TEN"] = 5.0 * exchangeRates["RUB"]!!
        // Запускаем поток для автоматического обновления курсов валют
        val executor = ScheduledThreadPoolExecutor(0)
        executor.scheduleAtFixedRate({
            exchangeRates["EUR"] = exchangeRates["EUR"]!! + ExchangeRateUpdate()
            exchangeRates["RUB"] = exchangeRates["RUB"]!! + ExchangeRateUpdate()
            exchangeRates["TEN"] = exchangeRates["TEN"]!! + ExchangeRateUpdate()
        }, 0, 1, TimeUnit.SECONDS)
    }
    }



// Представитель класса банк
class Bank {
    val clients = ConcurrentHashMap<Int, Client>()
    val observers = mutableListOf<Observer>()


    // Добавим наблюдателя
    fun addObserver(observer: Observer) {
        observers.add(observer)
    }


    // Уведомить всех наблюдателей
    fun notifyObservers(message: String) {
        observers.forEach {
            it.update(message)
        }
    }

    // Положить деньги на депозит
    fun deposit(clientId: Int, amount: Double) {
        val client = clients[clientId]
        val cashier =  Cashier(clientId, this)
        if (client != null && amount > 0) {
            cashier.deposit(clientId,amount)
            val message = "Клиент с id $clientId положил на депозит $amount ${client.currency}"
            notifyObservers(message)
        }
    }

    // Снятие денег со счёта
    fun withdraw(clientId: Int, amount: Double) {
        val client = clients[clientId]
        val cashier =  Cashier(clientId, this)
        if (client != null && amount > 0) {
            cashier.withdraw(clientId,amount)
            val message = "Клиент с id $clientId снял со счёта $amount ${client.currency}"
            notifyObservers(message)
        }
    }
    //  Перевод с счёта на счёт
    fun transferFunds(senderId: Int, receiverId: Int, amount: Double) {
        val SenderClient = clients[senderId]
        val ReceiverClient = clients[receiverId]
        val cashier =  Cashier(senderId, this)
        if (ReceiverClient != null) {
            if (SenderClient != null && amount > 0) {
                cashier.transferFunds(senderId, receiverId, amount)
                val message = "Клиент с id $senderId перевел клиенту с id $receiverId сумму $amount"
                notifyObservers(message)
            }
        }
    }
    // Произвести обмен валютой
    fun exchangeCurrency(clientId: Int, fromCurrency: String, toCurrency: String) {
        val client = clients[clientId]
        val cashier =  Cashier(clientId, this)
        cashier.exchangeCurrency(clientId, fromCurrency, toCurrency)
        val message = "Обмен успешен, клиент $clientId  ${client?.balance}  $toCurrency"
        notifyObservers(message)
        }



}

fun main() {
    // Считываем клиентов из файла
    val filePath = "/home/itech/Desktop/Project/Kotlin/Treadhomework/src/test/Input/input.txt"
    val line = Files.readString(Paths.get(filePath), Charsets.UTF_8).split(*arrayOf(" ", "\n"))

    // создаём экземпляр класса банк
    val bank = Bank()

    // создадим и зарегистрируем логгер в банке
    val logger = Logger()
    bank.addObserver(logger)
    // Создаем представителей класса клиенты
    for (i in 0..line.count()-3 step 3) {
        val clientId = line[i].toInt()
        val clientBalance = line[i+1].toDouble()
        val clientCurrency = line[i+2]
        val client = Client(clientId, clientBalance, clientCurrency)
        bank.clients[i] = client


    }
    // Пример работы
    println(bank.clients)
    bank.deposit(0, 500.0)
    println(bank.clients[0])

    bank.withdraw(0, 100.0)
    println(bank.clients[0])

    bank.exchangeCurrency(0, "RUB", "EUR")
    println(bank.clients[0])

    bank.transferFunds(3,6, 50.0)

    println(bank.clients[3])
    println(bank.clients[6])
}
