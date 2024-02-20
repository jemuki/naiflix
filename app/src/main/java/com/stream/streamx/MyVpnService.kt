package com.stream.streamx

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.widget.Toast
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.atomic.AtomicBoolean

class MyVpnService : VpnService() {
    private lateinit var vpnThread: Thread
    private lateinit var vpnInterface: ParcelFileDescriptor
    private val isRunning = AtomicBoolean(false)
    private lateinit var context: Context

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        context = this
        if (!isRunning.getAndSet(true)) {
            startVpnThread()
        }
        return START_STICKY
    }

    private fun startVpnThread() {
        vpnThread = Thread {
            try {
                establishVpnTunnel()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRunning.set(false)
            }
        }
        vpnThread.start()
    }

    private fun establishVpnTunnel() {
        val builder = Builder()
        vpnInterface = builder.setSession(getString(R.string.app_name))
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .establish()!!

        val input = FileInputStream(vpnInterface.fileDescriptor)
        val output = FileOutputStream(vpnInterface.fileDescriptor)

        val packet = ByteBuffer.allocate(32767)

        while (true) {
            // Read packet from the VPN interface
            val length = input.read(packet.array())

            if (length > 0) {
                // Process the packet (implement your filtering logic here)
                val processedPacket = processPacket(packet, length)

                // Write the processed packet back to the VPN interface
                output.write(processedPacket.array(), 0, length)
            }
            packet.clear()
        }
    }

    private fun processPacket(packet: ByteBuffer, length: Int): ByteBuffer {
        // Implement your logic to filter or modify the packet here
        // For example, block access to specific servers
        val packetData = ByteArray(length)
        packet.get(packetData)

        val packetString = String(packetData)
        val blockedServers = getBlockedServersFromAssets()
        for (serverAddress in blockedServers) {
            if (packetString.contains(serverAddress)) {
                // Block access to the specific server
                showToast("Blocked server: $serverAddress")
                return ByteBuffer.allocate(0)
            }
        }

        // No blocking, return the original packet
        return ByteBuffer.wrap(packetData)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun getBlockedServersFromAssets(): List<String> {
        val blockedServers = mutableListOf<String>()
        try {
            val inputStream = assets.open("sp.txt")
            val serverData = inputStream.bufferedReader().readLines()
            blockedServers.addAll(serverData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return blockedServers
    }

    override fun onDestroy() {
        stopVpnThread()
        super.onDestroy()
    }

    private fun stopVpnThread() {
        isRunning.set(false)
        vpnThread.interrupt()
        vpnInterface.close()
    }
}
