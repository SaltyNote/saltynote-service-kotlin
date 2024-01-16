package com.saltynote.service.generator

import com.saltynote.service.exception.ClockBackwardException


class SnowflakeIdGenerator(dcId: Long, mId: Long) : IdGenerator {

    companion object {
        /**
         * 起始的时间戳: 05/05/2022 20:16
         */
        private const val START_STAMP = 1651806960000L

        /**
         * 每一部分占用的位数
         */
        private const val SEQUENCE_BIT: Int = 13 // 序列号占用的位数

        private const val MACHINE_BIT: Int = 4 // 机器标识占用的位数

        private const val DATACENTER_BIT: Int = 3 // 数据中心占用的位数


        /**
         * 每一部分的最大值
         */
        private const val MAX_DATACENTER_NUM = (-1L shl DATACENTER_BIT).inv()

        private const val MAX_MACHINE_NUM = (-1L shl MACHINE_BIT).inv()

        private const val MAX_SEQUENCE = (-1L shl SEQUENCE_BIT).inv()

        /**
         * 每一部分向左的位移
         */
        private const val MACHINE_LEFT = SEQUENCE_BIT

        private const val DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT

        private const val TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT

        val DEFAULT = SnowflakeIdGenerator(1, 1)
    }

    private var datacenterId: Long = 0 // 数据中心
    private var machineId: Long = 0 // 机器标识
    private var sequence = 0L // 序列号
    private var lastStamp = -1L // 上一次时间戳

    init {
        require(!(dcId > MAX_DATACENTER_NUM || dcId < 0)) { "datacenterId can't be greater than $MAX_DATACENTER_NUM or less than 0" }
        require(!(mId > MAX_MACHINE_NUM || mId < 0)) { "machineId can't be greater than $MAX_MACHINE_NUM or less than 0" }
        datacenterId = dcId
        machineId = mId
    }

    /**
     * 产生下一个ID
     */
    @Synchronized
    override fun nextId(): Long {
        var currStamp = getCurrentTimestamp()
        if (currStamp < lastStamp) {
            throw ClockBackwardException("Clock moved backwards.  Refusing to generate id")
        }
        if (currStamp == lastStamp) {
            // 相同毫秒内，序列号自增
            sequence = sequence + 1 and MAX_SEQUENCE
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill()
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L
        }
        lastStamp = currStamp

        // 时间戳部分 | 数据中心部分 | 机器标识部分 | 序列号部分
        return ((currStamp - START_STAMP) shl TIMESTAMP_LEFT) or (datacenterId shl DATACENTER_LEFT) or (machineId shl MACHINE_LEFT) or sequence
    }

    private fun getNextMill(): Long {
        var mill = getCurrentTimestamp()
        while (mill <= lastStamp) {
            mill = getCurrentTimestamp()
        }
        return mill
    }

    private fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

}
