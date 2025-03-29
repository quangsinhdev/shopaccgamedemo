package com.shopaccgame.services.user.transaction.deposit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import com.shopaccgame.config.VNPayConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

	public String createOrder(int total, String orderInfor, String urlReturn) {
		String vnp_Version = "2.1.0";
		String vnp_Command = "pay";
		String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
		String vnp_IpAddr = "127.0.0.1";
		String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
		String orderType = "order-type";

		Map<String, String> vnp_Params = new HashMap<>();
		vnp_Params.put("vnp_Version", vnp_Version);
		vnp_Params.put("vnp_Command", vnp_Command);
		vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
		vnp_Params.put("vnp_Amount", String.valueOf(total * 100));
		vnp_Params.put("vnp_CurrCode", "VND");
		vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
		vnp_Params.put("vnp_OrderInfo", orderInfor);
		vnp_Params.put("vnp_OrderType", orderType);

		String locate = "vn";
		vnp_Params.put("vnp_Locale", locate);
		vnp_Params.put("vnp_ReturnUrl", urlReturn);
		vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

		Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String vnp_CreateDate = formatter.format(cld.getTime());
		vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

		cld.add(Calendar.MINUTE, 15);
		String vnp_ExpireDate = formatter.format(cld.getTime());
		vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

		List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
		Collections.sort(fieldNames);
		StringBuilder hashData = new StringBuilder();
		StringBuilder query = new StringBuilder();
		Iterator<String> itr = fieldNames.iterator();
		while (itr.hasNext()) {
			String fieldName = itr.next();
			String fieldValue = vnp_Params.get(fieldName);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
				query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
						.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
				if (itr.hasNext()) {
					query.append('&');
					hashData.append('&');
				}
			}
		}
		String queryUrl = query.toString();
		String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
		queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
		return VNPayConfig.vnp_PayUrl + "?" + queryUrl;
	}

	public int orderReturn(HttpServletRequest request) {
		Map<String, String> fields = new HashMap<>();
		for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
			String fieldName = params.nextElement();
			String fieldValue = request.getParameter(fieldName);
			fieldName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
			fieldValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
			if ((fieldValue != null) && (fieldValue.length() > 0)) {
				fields.put(fieldName, fieldValue);
			}
		}

		String vnp_SecureHash = request.getParameter("vnp_SecureHash");

		if (fields.containsKey("vnp_SecureHashType")) {
			fields.remove("vnp_SecureHashType");
		}
		if (fields.containsKey("vnp_SecureHash")) {
			fields.remove("vnp_SecureHash");
		}

		String signValue = VNPayConfig.hashAllFields(fields);

		if (signValue.equals(vnp_SecureHash)) {
			String transactionStatus = request.getParameter("vnp_TransactionStatus");
			return "00".equals(transactionStatus) ? 1 : 0;
		} else {
			return -1;
		}
	}
}