package com.svega.vanitygen

import java.text.DecimalFormat
import java.time.Duration
import java.util.*

class CLIHandler(scanner: Scanner?): ProgressUpdatable {
    private var addresses = 0
    private var complexity = 0L
    private var iters = ""
    private var addressesPerSecond = ""
    private var elapsedSeconds = 0L
    private var timeElapsed = ""
    private var decimalPlaces = 0
    private var expectedPctEffort = ""
    private var expectedTimeRemaining = ""
    private var expectedIters = ""
    private var done = false
        set(new){
            println("done set")
            for(trace in Thread.currentThread().stackTrace){
                println(trace.toString())
            }
        }
    private lateinit var vgs: VanityGenState
    init{
        if(scanner != null) {
            val cThr = Thread(Runnable {
                println("start console thread")
                while(!done){
                    println("wait for line")
                    val line = scanner.next()!!
                    when {
                        line.startsWith("help") -> {
                            println("Enter 'inc-gen' to increase generator threads")
                            println("Enter 'dec-gen' to decrease generator threads")
                            println("Enter 'status' to see status")
                        }
                        line.startsWith("inc-gen", true) -> vgs.increaseGenThreads()
                        line.startsWith("dec-gen", true) -> vgs.decreaseGenThreads()
                        line.startsWith("status", true) -> {
                            println(iters)
                            println(addressesPerSecond)
                            println(timeElapsed)
                            println(expectedPctEffort)
                            println(expectedTimeRemaining)
                            println(expectedIters)
                        }
                    }
                    println("Post-when: $done")
                }
                println("Outside loop")
            })
            cThr.name = "CLI Console Thread"
            cThr.start()
        }else{
            println("scanner is null")
        }
    }
    override fun update(item: UpdateItem, data: Any) {
        when (item) {
            UpdateItem.NUMBER_GEN -> {
                addresses = data as Int
                iters = "Iterations so far: " + DecimalFormat("#,###").format(data)
                val exp = 100 * addresses.toDouble() / complexity.toDouble()
                val pctEff = String.format("%1.${decimalPlaces}f%%", exp)
                expectedPctEffort = "Percentage effort based on expectations: $pctEff"
            }
            UpdateItem.WARN_TEXT -> println(data as String)
            UpdateItem.VAL_THREADS -> println("Increased number of validation threads to $data")
            UpdateItem.GEN_THREADS -> println("Increased number of generation threads to $data")
            UpdateItem.ADDRESS -> {
                //addressStr = data as String
                //address.setText("Address is: $`in`")
            }
            UpdateItem.SEED -> {}//seed.setText("Seed is: $`in`")
            UpdateItem.ADDRESSES_PER_SEC -> {
                addressesPerSecond = "Addresses generated per second: ${data as Long}"
                val expectedSecs = (complexity - addresses) / data + elapsedSeconds
                expectedTimeRemaining = "Expected to take " + Utils.formatDuration(Duration.ofSeconds(expectedSecs))
            }
            UpdateItem.STATUS -> println(data as String)
            UpdateItem.TIME -> {
                elapsedSeconds = Duration.ofSeconds(data as Long).seconds
                timeElapsed = "Time elapsed: " + Utils.formatDuration(Duration.ofSeconds(data))
            }
            UpdateItem.COMPLEXITY -> {
                expectedIters = "Expected number of iterations to make: ${data as Long}"
                decimalPlaces = Math.max(1, Math.log10(complexity.toDouble()).toInt() / 2)
            }
            UpdateItem.MNEMONIC -> {
                //mnemonicStr = `in` as String
                //mnemonic.setText("Mnemonic is: $`in`")
                //start.setText("Begin matching")
            }
            UpdateItem.QDEPTH -> {}//qDepth.setText("Queue depth: $`in`")
            UpdateItem.POST_GEN -> done = true
            UpdateItem.SELF -> vgs = data as VanityGenState
        }
    }
}