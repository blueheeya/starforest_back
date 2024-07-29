package com.project.starforest.controller;


import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.starforest.domain.CampImage;
import com.project.starforest.domain.CampSite;
import com.project.starforest.domain.ReservInfo;
import com.project.starforest.domain.Reservation;
import com.project.starforest.domain.ReservationDates;
import com.project.starforest.dto.CampReservationInfoDTO;
import com.project.starforest.dto.CampSearchDTO;
import com.project.starforest.dto.KakaoPayReadyResponse;
import com.project.starforest.dto.MapResponseDTO;
import com.project.starforest.dto.PaymentApprovalResponse;
import com.project.starforest.dto.ReservationDto;
import com.project.starforest.dto.ReservationInfoDTO;
import com.project.starforest.dto.ReservationInfoPayDTO;
import com.project.starforest.dto.ViewMapResponseDTO;
import com.project.starforest.repository.CampImageRepository;
import com.project.starforest.repository.MapTestRepository;
import com.project.starforest.repository.PointRepository;
import com.project.starforest.repository.ReservInfoRepository;
import com.project.starforest.repository.ReservationRepository;
import com.project.starforest.repository.ReservationedRepository;
import com.project.starforest.service.impl.CampReservPayService;
import com.project.starforest.service.impl.CampSearchService;
import com.project.starforest.service.impl.CoordinatesService;
import com.project.starforest.service.impl.KakaoPayService;


@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/camp")
public class CampController {
	
	//-----------吏��룄----------//

		@Autowired
		private MapTestRepository mapTestRepository;
		
		@Autowired
		private CampImageRepository campImageRepository;
		
		@PostMapping("/view/{id}")
		public ResponseEntity<ViewMapResponseDTO> viewMap(
				@PathVariable("id") Long id
				) {
			
			CampSite entity = mapTestRepository.findById(id).orElseThrow();
			List<CampImage> campImageEntity = campImageRepository.findByCampId(id);
			
			ViewMapResponseDTO result = ViewMapResponseDTO.builder()
					.id(entity.getId())
					.name(entity.getName())
					.lineIntro(entity.getLine_intro())
					.intro(entity.getIntro())
					.allar(entity.getAllar())
					.featureNm(entity.getFeature_nm())
					.is_glamp(entity.is_glamp())
					.is_carvan(entity.is_carvan())
					.is_auto(entity.is_auto())
					.sigunguNm(entity.getSigungu_nm())
					.mapx(entity.getMapx())
					.mapy(entity.getMapy())
					.tel(entity.getTel())
					.homepage(entity.getHomepage())
					.price(entity.getPrice())
					.animalCmgCl(entity.getAnimal_cmg_cl())
					.glampInnerFclty(entity.getGlamp_inner_fclty())
					.caravInnerFclty(entity.getCarav_inner_fclty())
					.posblFcltyCl(entity.getPosbl_fclty_cl())
					.themaEnvrnCl(entity.getThema_envrn_cl())
					.sbrsCl(entity.getSbrs_cl())
					.eqpmnLendCl(entity.getEqpmn_lend_cl())
					.first_image_url(entity.getFirst_image_url())
					.add1(entity.getAdd1())
					.add2(entity.getAdd2())
					.brazierCl(entity.getBrazier_cl())
					.campImages(campImageEntity)
					.build();
			log.info(result.toString());
			return ResponseEntity.ok(result);
		}	
	

		@Autowired
		private CoordinatesService coordinatesService;
		
		@Autowired
		private PointRepository pointRepository;

		@PostMapping("/coordinates")
	    public ResponseEntity<List<MapResponseDTO>> getNearbyPoints(@RequestBody CampSite mapEntity) {

	        List<CampSite> result = coordinatesService.findNearbyPoints(mapEntity);
	        
//	        log.info("result"+result.toString());
	        
	        //寃곌낵媛� �뾾�쓣�븣
	        if(result.isEmpty()) {
	        	return ResponseEntity.ok(null);
	        }
	        
	     // MapEntity 由ъ뒪�듃瑜� MapResponseDTO 由ъ뒪�듃濡� 蹂��솚�뀅�뀕
	        List<MapResponseDTO> response = result.stream()
	                .map(entity -> MapResponseDTO.builder()
	                        .id(entity.getId())
	                        .name(entity.getName())
	                        .first_image_url(entity.getFirst_image_url())
	                        .add1(entity.getAdd1())
	                        .is_auto(entity.is_auto())
	                        .is_carvan(entity.is_carvan())
	                        .is_glamp(entity.is_glamp())
	                        .homepage(entity.getHomepage())
	                        .price(entity.getPrice())
	                        .mapX(entity.getMapx())
	                        .mapY(entity.getMapy())
	                        .build())
	                .collect(Collectors.toList());
	 
	        
	        
	        return ResponseEntity.ok(response);
	    }
		
		
//-------------------예약 정보------------------//
		@GetMapping("/reservation/infos/{id}")
		public ResponseEntity<ReservationInfoPayDTO> payReservationinfo(
				@PathVariable("id") Long id
				) {
			LocalDateTime now = LocalDateTime.now();
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	        String formattedDate = now.format(formatter);
	        double random = Math.random()*100;
	        int randomInt = (int)Math.floor(random);
	        String combined = formattedDate + randomInt;
	        log.info("123123123123"+combined);
			
			CampSite entity = mapTestRepository.findById(id).orElseThrow();
			ReservationInfoPayDTO dto = ReservationInfoPayDTO.builder()
					.id(entity.getId())
					.reservNum(combined)
					.name(entity.getName())
					.price(entity.getPrice())
					.build();
			return ResponseEntity.ok(dto);
		}
//-------------------예약 정보------------------//
		
		
		
	//--------------移댁뭅�삤 寃곗젣----------------//

//		
		  private final KakaoPayService kakaoPayService;
//		  private final PaymentRepository paymentRepository;
//
		    @PostMapping("/kakaoPay/{reservId}")
		    public KakaoPayReadyResponse kakaoPay(
		    		@RequestBody ReservationInfoDTO dto,
		    		@PathVariable("reservId") Long reservId
		    		) {
		    	log.info("카카오 페이 요청");
		    	log.info(dto.toString());
		        return kakaoPayService.kakaoPayReady(dto,reservId);
		    }
		    
		    @Autowired
		    private ReservationedRepository reservationedRepository; 
		    
		    @Autowired
		    private ReservationRepository reservationRepository;
		    
		    @Autowired
		    private ReservInfoRepository reservInfoRepository;
	    
		    @GetMapping("/kakaoPaySuccess/{pg_token}/{reservNum}/{reservId}/{name}/{carNum}/{request}/{tel}")
		    public ResponseEntity<PaymentApprovalResponse> kakaoPaySuccess(
		    		@PathVariable("pg_token") String pgToken,
		    		@PathVariable("reservNum") String reservNum,
		    		@PathVariable("reservId") Long reservId,
		    		@PathVariable("name") String name,
		    		@PathVariable("carNum") String carNum,
		    		@PathVariable("request") String request,
		    		@PathVariable("tel") String tel
		    		) {
		    	log.info("reservId"+reservId);
		        // 1. 移댁뭅�삤�럹�씠 API濡� 寃곗젣 �듅�씤 �슂泥�
		        PaymentApprovalResponse approvalResponse = kakaoPayService.approvePayment(pgToken,reservNum);
		        
			        log.info("결제 성공");
			        
			        
			        //reservation에 is_payment ture로 변경하기+예약번호 저장하기
			        Reservation entity = reservationedRepository.findById(reservId).orElseThrow();
			        entity.changeReservation_number(reservNum);
			        entity.changeIs_payment(true);
			        
			        reservationedRepository.save(entity);
			        
			        //결제 성공시 reservationData에 정보 저장하기
			        ReservationDates dateEntity = ReservationDates.builder()
			        		.campSite(entity.getCampsite_id())
			        		.created_at(LocalDateTime.now())
			        		.end_date(entity.getEnd_date())
			        		.start_date(entity.getStart_date())
			        		.build();
			        
			        reservationRepository.save(dateEntity);
			        
			        //reserv_info에 예약자 정보 저장하기
			        ReservInfo InfoEntity = ReservInfo.builder()
			        		.reservation(entity)
			        		.car_number(carNum)
			        		.name(name)
			        		.request(request)
			        		.tel(tel)
			        		.build();
			        
			        reservInfoRepository.save(InfoEntity);
		        
		        // 3. �겢�씪�씠�뼵�듃�뿉 �븘�슂�븳 �젙蹂� 諛섑솚
		        return ResponseEntity.ok(approvalResponse);
		    }
	
	
	//----------------�삁�빟-----------------//
		


		//�뜲�씠�꽣 由ъ뒪�듃 蹂닿린
		@GetMapping("/reservations/{id}")
		   public ResponseEntity<List<ReservationDto>> getAllReservations(
				   @PathVariable("id") Long id
				   ) {
			List<ReservationDates> results = reservationRepository.findByCampId(id);
			List<ReservationDto> result = results.stream().map(entity->ReservationDto.builder()
					.id(id)
					.startDate(entity.getStart_date())
					.endDate(entity.getEnd_date())
					.createdAt(entity.getCreated_at())
					.build())
					.collect(Collectors.toList());
					
		       return ResponseEntity.ok(result);
			}
		    
		//예약 날짜 확인후 reservation에 저장(is_payment는 false)
		@Autowired
		private CampReservPayService campReservPayService;
		
		//예약하기 버튼 클릭시 실행하는곳
		@PostMapping("/reservation/{id}")
		public ResponseEntity<Reservation> createReservation(
		 		@RequestBody ReservationDto dto,
		 		@PathVariable("id") Long id
		   		) {
			
		   	return campReservPayService.getIsCampReservation(dto,id);
		}
		
	
	//-------------------캠핑장 검색---------------------//

		@Autowired
		private CampSearchService campSearchService;
		
		@GetMapping("/search")
		public ResponseEntity<List<CampSearchDTO>> getfindcamp(
				@RequestParam("query") String query
				) {
			
			List<CampSearchDTO> result = campSearchService.searchCamp(query);
			
					
			return ResponseEntity.ok(result);
		}
	
	//------------------�삁�빟------------------------//

		
		@GetMapping("/reservation/{id}")
		public ResponseEntity<CampReservationInfoDTO> getCampReservation(
				@PathVariable("id") Long id
				) {
			
			CampSite entity = mapTestRepository.findById(id).orElseThrow();
			//s
			CampReservationInfoDTO result = CampReservationInfoDTO.builder()
					.id(entity.getId())
					.is_glamp(entity.is_glamp())
					.is_carvan(entity.is_carvan())
					.is_auto(entity.is_auto())
					.first_image_url(entity.getFirst_image_url())
					.name(entity.getName())
					.add1(entity.getAdd1())
					.mapx(entity.getMapx())
					.mapy(entity.getMapy())
					.sigungu_nm(entity.getSigungu_nm())
					.build(); 
			
			return ResponseEntity.ok(result);
		}
		
		@GetMapping("/view/map/{id}")
		public ResponseEntity<CampSite> getCampViewMap(
				@PathVariable("id") Long id
				) {
			CampSite result = mapTestRepository.findById(id).orElseThrow();
			
			return ResponseEntity.ok(result);
		}
}