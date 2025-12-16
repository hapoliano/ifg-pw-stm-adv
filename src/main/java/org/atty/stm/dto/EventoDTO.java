package org.atty.stm.dto;

public class EventoDTO {
    private Long id;
    private String title;
    private String description;
    private String start;
    private String end;
    private String color;
    private String tipo;
    private Long processoId;
    // NOVO CAMPO: allDay para o FullCalendar
    private boolean allDay;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public String getEnd() { return end; }
    public void setEnd(String end) { this.end = end; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Long getProcessoId() { return processoId; }
    public void setProcessoId(Long processoId) { this.processoId = processoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    // NOVO: Getter/Setter para allDay
    public boolean isAllDay() { return allDay; }
    public void setAllDay(boolean allDay) { this.allDay = allDay; }
}