package com.cegesoft.log;

import java.util.ArrayList;
import java.util.List;

/**
 * Génère une barre de progression
 */
public class ProgressBar {
    private final int progressBarWidth;
    private final List<PBField> fields = new ArrayList<>();

    private int lastDone = 0;
    private int lastTotal = 0;

    public ProgressBar(int progressBarWidth) {
        this.progressBarWidth = progressBarWidth;
    }

    public ProgressBar() {
        this(100);
    }

    public void addField(PBField field) {
        fields.add(field);
    }

    public void removeField(PBField field) {
        fields.remove(field);
    }

    private int getDone() {
        int done = 0;
        for (PBField field : fields) {
            done += field.getIndex();
        }
        return done;
    }

    private int getTotal() {
        int total = 0;
        for (PBField field : fields) {
            total += field.getTotal();
        }
        return total;
    }

    public String update() {
        int done = getDone();
        int total = getTotal();
        this.lastDone = done;
        this.lastTotal = total;
        float percent = (progressBarWidth * ((float) done / (float) total));
        StringBuilder buffer = new StringBuilder(progressBarWidth + 10);
        buffer.append("[");
        for (int i = 0; i < progressBarWidth; i++) {
            if (i < (int) percent || (done + 1 == total)) {
                buffer.append("=");
            } else if (i == (int) percent) {
                buffer.append(">");
            } else {
                buffer.append(" ");
            }
        }
        buffer.append("] ").append(Math.round((percent * 100.0 / progressBarWidth) * 10) / 10.0).append("%")
                .append(" (").append(done + 1).append("/").append(total).append(")");
        if (done + 1 == total) {
            buffer.append("\n");
        }
        return "\r" + buffer;
    }

    public boolean isValid() {
        return this.fields.size() != 0;
    }

    public boolean isFinished() {
        return this.getDone() + 1 == this.getTotal();
    }

    public boolean hasChanged() {
        return this.lastDone != this.getDone() || this.lastTotal != this.getTotal();
    }

    public interface PBField {
        int getIndex();
        int getTotal();
    }

}
