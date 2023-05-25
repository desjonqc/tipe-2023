package com.cegesoft.util;

public class ProgressBar {
    private static final int progressBarWidth = 100;
    public static void printProgress(int done, int total) {
        float percent = (progressBarWidth * ((float) done / (float) total));
        StringBuffer buffer = new StringBuffer(progressBarWidth + 10);
        buffer.append("[");
        for (int i = 0; i < progressBarWidth; i++) {
            if (i < (int)percent || (done + 1 == total)) {
                buffer.append("=");
            } else if (i == (int)percent) {
                buffer.append(">");
            } else {
                buffer.append(" ");
            }
        }
        buffer.append("] ").append(Math.round((percent * 100.0 / progressBarWidth) * 10) / 10.0).append("%");
        if (done + 1 == total) {
            buffer.append("\n");
        }
        System.out.print("\r" + buffer);
    }

}
