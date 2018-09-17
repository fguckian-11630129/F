package hotel.booking;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import hotel.booking.BookingUI.State;
import hotel.credit.CreditAuthorizer;
import hotel.credit.CreditCard;
import hotel.credit.CreditCardType;
import hotel.entities.Booking;
import hotel.entities.Guest;
import hotel.entities.Hotel;
import hotel.entities.Room;
import hotel.entities.RoomType;
import hotel.utils.IOUtils;

public class BookingCTL {
	
	
	private static enum CONTROL_STATE {PHONE, ROOM, REGISTER, TIMES, CREDIT, APPROVED, CANCELLED, COMPLETED}	
	
	private BookingUI bookingUI;
	private Hotel hotel;

	private Guest guest;
	private Room room;
	private double cost;
	
	private CONTROL_STATE controlState;
	private int phoneNumber;
	private RoomType selectedRoomType;
	private int occupantNumber;
	private Date arrivalDate;
	private int stayLength;

	
	public BookingCTL(Hotel hotel) {
		this.bookingUI = new BookingUI(this);
		this.hotel = hotel;
		controlState = CONTROL_STATE.PHONE;
	}

	
	public void run() {		
		IOUtils.trace("BookingCTL: run");
		bookingUI.run();
	}
	
	
	public void phoneNumberEntered(int phoneNumber) {
		if (controlState != CONTROL_STATE.PHONE) {
			String mesg = String.format("BookingCTL: phoneNumberEntered : bad state : %s", controlState);
			throw new RuntimeException(mesg);
		}
		this.phoneNumber = phoneNumber;
		
		boolean isRegistered = hotel.isRegistered(phoneNumber);
		
		if (isRegistered) {
			guest = hotel.findGuestByPhoneNumber(phoneNumber);
			bookingUI.displayGuestDetails(guest.getName(), guest.getAddress(), guest.getPhoneNumber());
			this.controlState = CONTROL_STATE.ROOM;
			bookingUI.setState(BookingUI.State.ROOM);
		}
		else {
			this.controlState = CONTROL_STATE.REGISTER;
			bookingUI.setState(BookingUI.State.REGISTER);
		}
	}


	public void guestDetailsEntered(String name, String address) {
		if (controlState != CONTROL_STATE.REGISTER) {
			String mesg = String.format("BookingCTL: guestDetailsEntered : bad state : %s", controlState);
			throw new RuntimeException(mesg);
		}
		guest = hotel.registerGuest(name, address, phoneNumber);
		
		bookingUI.displayGuestDetails(guest.getName(), guest.getAddress(), guest.getPhoneNumber());
		controlState = CONTROL_STATE.ROOM;
		bookingUI.setState(BookingUI.State.ROOM);
	}


	public void roomTypeAndOccupantsEntered(RoomType selectedRoomType, int occupantNumber) {
		if (controlState != CONTROL_STATE.ROOM) {
			String mesg = String.format("BookingCTL: roomTypeAndOccupantsEntered : bad state : %s", controlState);
			throw new RuntimeException(mesg);
		}
		this.selectedRoomType = selectedRoomType;
		this.occupantNumber = occupantNumber;
		
		boolean suitable = selectedRoomType.isSuitable(occupantNumber);
		
		if (!suitable) {			
			String notSuitableMessage = "\nRoom type unsuitable, please select another room type\n";
			bookingUI.displayMessage(notSuitableMessage);
		}
		else {
			controlState = CONTROL_STATE.TIMES;
			bookingUI.setState(BookingUI.State.TIMES);
		}
	}


	public void bookingTimesEntered(Date arrivalDate, int stayLength) {
		if (controlState != CONTROL_STATE.TIMES) {
			String mesg = String.format("BookingCTL: bookingTimesEntered : bad state : %s", controlState);
			throw new RuntimeException(mesg);
		}
		this.arrivalDate = arrivalDate;
		this.stayLength = stayLength;
		
		room = hotel.findAvailableRoom(selectedRoomType, arrivalDate, stayLength);
		
		if (room == null) {				
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(arrivalDate);
			calendar.add(Calendar.DATE, stayLength);
			Date departureDate = calendar.getTime();
			
			String notAvailableStr = String.format("\n%s is not available between %s and %s\n",
					selectedRoomType.getDescription(),
					format.format(arrivalDate),
					format.format(departureDate));
			
			bookingUI.displayMessage(notAvailableStr);
		}
		else {
			cost = selectedRoomType.calculateCost(arrivalDate, stayLength);
			String description = selectedRoomType.getDescription();
			bookingUI.displayBookingDetails(description, arrivalDate, stayLength, cost);
			controlState = CONTROL_STATE.CREDIT;
			bookingUI.setState(BookingUI.State.CREDIT);
		}
	}


	public void creditDetailsEntered(CreditCardType type, int number, int ccv) {
		if(controlState!=controlState.CREDIT) {
			throw new RuntimeException("Booking state must be in CREDIT in order to enter details");
		}
		CreditCard creditCard = new CreditCard(type, number, ccv);
		if(CreditAuthorizer.getInstance().authorize(creditCard, cost)) {
			//book hotel
			long confirmationNumber = hotel.book(room, guest, arrivalDate, ccv, number, creditCard);
			
			//call UI.displayConfirmedBooking
			Booking booking = hotel.bookingsByConfirmationNumber.get(confirmationNumber);
			Room room = booking.getRoom();
			String roomDescription = room.getDescription();
			int roomNumber = room.getId();
			Guest guest = booking.getGuest();
			String guestName = guest.getName();
			String creditCardVendor = creditCard.getVendor();
			int cardNumber = creditCard.getNumber();
			bookingUI.displayConfirmedBooking(roomDescription, roomNumber, arrivalDate, ccv, guestName, creditCardVendor, cardNumber, ccv, confirmationNumber);
			
			//update states
			this.controlState = controlState.COMPLETED;
			bookingUI.setState(BookingUI.State.COMPLETED);
		}
		else {
			bookingUI.displayMessage("Card could not be authorised");
		}
	}


	public void cancel() {
		IOUtils.trace("BookingCTL: cancel");
		bookingUI.displayMessage("Booking cancelled");
		controlState = CONTROL_STATE.CANCELLED;
		bookingUI.setState(BookingUI.State.CANCELLED);
	}
	
	
	public void completed() {
		IOUtils.trace("BookingCTL: completed");
		bookingUI.displayMessage("Booking completed");
	}

	

}
