# KDF_Treads

fun main() {
    // считаем клиентов из файла
    val filePath = "/home/itech/Desktop/Project/Kotlin/Treadhomework/src/test/Input/input.txt"
    val line = Files.readString(Paths.get(filePath), Charsets.UTF_8).split(*arrayOf(" ", "\n"))
    // создаём экземпляр класса банк
    val bank = Bank()


    // создадим и зарегистрируем логгер в банке
    val logger = Logger()
    bank.addObserver(logger)

    for (i in 0..line.count()-3 step 3) {
        val clientId = line[i].toInt()
        val clientBalance = line[i+1].toDouble()
        val clientCurrency = line[i+2]
        val client = Client(clientId, clientBalance, clientCurrency)
        bank.clients[i] = client
        val cashier = Cashier(clientId,bank)
        //cashier.deposit(clientId,100.0)


    }
    println(bank.clients)
    bank.deposit(0, 500.0)
    println(bank.clients[0])

    bank.withdraw(0, 100.0)
    println(bank.clients[0])

    bank.exchangeCurrency(0, "RUB", "EUR")
    println(bank.clients[0])
    bank.transferFunds(3,4, 50.0)
    println(bank.clients[3])
    println(bank.clients[4])
}
