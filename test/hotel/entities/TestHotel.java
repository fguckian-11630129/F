package hotel.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;

import hotel.credit.CreditCard;


@ExtendWith(MockitoExtension.class)
class TestHotel {

	@Mock Guest mockGuest;
	Date arrivalDate;
	int stayLength;
	int occupantNumber;
	@Mock CreditCard mockCreditCard;
	@Mock Room mockRoom;
	@Spy Map<Long, Booking> bookingsByConfirmationNumber = new HashMap<Long, Booking>();
	SimpleDateFormat format;
	@Mock Booking mockBooking;
	long confirmationNumber;
	
	@BeforeEach
	void setUp() throws Exception {
		stayLength = 3;
		occupantNumber = 1;
		format = new SimpleDateFormat("dd-MM-yyyy");
		arrivalDate = format.parse("11-11-2018");
		confirmationNumber = 1112018101L;
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@InjectMocks Hotel hotel;
	
	@Test
	void testBookAllValid() {
		//arrange
		when(mockRoom.book(mockGuest, arrivalDate, stayLength, occupantNumber, mockCreditCard)).thenReturn(mockBooking);
		when(mockBooking.getConfirmationNumber()).thenReturn(confirmationNumber);
		assertEquals(0, hotel.bookingsByConfirmationNumber.size());
		
		//act  
		long actual = hotel.book(mockRoom, mockGuest, arrivalDate, stayLength, occupantNumber, mockCreditCard);
		
		//assert
		verify(mockRoom.book(mockGuest, arrivalDate, stayLength, occupantNumber, mockCreditCard));
		assertEquals(actual, confirmationNumber);
		assertEquals(1, hotel.bookingsByConfirmationNumber);
		
	}

	@Test
	void testBookRoomBookThrowsException() {
		//arrange
		when(mockRoom.book(mockGuest, arrivalDate, stayLength, occupantNumber, mockCreditCard))
			.thenThrow(new RuntimeException("Error Message"));
		assertEquals(0, hotel.bookingsByConfirmationNumber.size());
		
		//act 
		Executable e = () -> hotel.book(mockRoom, mockGuest, arrivalDate, stayLength, occupantNumber, mockCreditCard);
		Throwable t = assertThrows(RuntimeException.class, e);
		
		//assert
		verify(mockRoom.book(mockGuest, arrivalDate, stayLength, occupantNumber, mockCreditCard));
		assertEquals(0, hotel.bookingsByConfirmationNumber);
		assertEquals("Error Message", t.getMessage());
		
	}
	
}


