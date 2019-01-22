package io.narayana.demo.lra.devconf2019.jpa;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "FLIGHTS")
@NamedQueries({
    @NamedQuery(name="Flight.findAll", query="SELECT f FROM Flight f"),
    @NamedQuery(name="Flight.findByDate", query="SELECT f FROM Flight f WHERE f.date = :date")
})
public class Flight implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "flight")
    private List<Booking> bookings = new ArrayList<>();

    private Date date;
    private int numberOfSeats, bookedSeats;

    public int getId() {
        return id;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public Flight setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
        return this;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public Flight setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public String getDateFormated() {
        return new SimpleDateFormat(DATE_FORMAT).format(getDate());
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return String.format("[%s] date: %s, seats: %d, booked: %d",
                id, getDateFormated(), numberOfSeats, bookedSeats);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + bookedSeats;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + id;
        result = prime * result + numberOfSeats;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Flight other = (Flight) obj;
        if (bookedSeats != other.bookedSeats)
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (id != other.id)
            return false;
        if (numberOfSeats != other.numberOfSeats)
            return false;
        return true;
    }
}
