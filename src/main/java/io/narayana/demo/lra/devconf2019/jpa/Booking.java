package io.narayana.demo.lra.devconf2019.jpa;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "BOOKINGS")
@NamedQueries({
    @NamedQuery(name="Booking.findAll", query="SELECT b FROM Booking b"),
    @NamedQuery(name="Booking.findByLraId", query="SELECT b FROM Booking b WHERE b.lraId = :lraId")
})
public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id")
    private Flight flight;

    private String name;
    private BookingStatus status = BookingStatus.IN_PROGRESS;
    private String lraId;
    
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Booking setName(String name) {
        this.name = name;
        return this;
    }

    public Flight getFlight() {
        return flight;
    }

    public Booking setFlight(Flight flight) {
        this.flight = flight;
        return this;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getLraId() {
        return lraId;
    }

    public Booking setLraId(String lraId) {
        this.lraId = lraId;
        return this;
    }

    @Override
    public String toString() {
        return String.format("[%d] passenger name: %s, go by flight: '%d,%s', booking status: %s",
                id, name, flight.getId(), flight.getDateFormated(), status);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flight == null) ? 0 : flight.hashCode());
        result = prime * result + id;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Booking other = (Booking) obj;
        if (flight == null) {
            if (other.flight != null)
                return false;
        } else if (!flight.equals(other.flight))
            return false;
        if (id != other.id)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
