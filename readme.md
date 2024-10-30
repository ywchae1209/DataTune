### What for ?

This is a illustrative sample program of `ArkFlow`. (but, not too simple)

----
using
* kafka
* zookeeper (for run-time control)
* elasticSearch

----
* scala (language)
* akka-stream ( stream-control )

### How-to


#### 1. setup docker desktop   

https://www.docker.com/products/docker-desktop/

In this scenario we'll use docker-container

#### 2. run container
```
docker-compose -f docker.yml up
```
* docker.yml : located at resources directory.
* this will create and start container. 
    - kafka( port:9092)
    - zookeeper( port:2181)
    - elasticSearch( port:9200)
    - kibana (UI for elasticSearch port:5601)
    - kafka-ui (UI for kafka port:8080)
    - zoo-navigator( UI for zookeeper port:9000) connection String = zookeeper
     
#### 3. insert some nodes and write-rules

with zoo-navigator UI ( connect `localhost:9000` with web-browser)

* classify (illustrative)
-----
node-path :: `/dataTune/classify`
```json
[
  {
    "id": 1,
    "rule": "'TRT_INFO' & 'ISR_INFO' & 'RCPT_HEADER' & 'RCPT_DETAIL' & 'FEE_DETAIL' & 'PRS_INFO'"
  }
]
```

* validate (illustrative)
-----
node-path :: `/dataTune/validate`
```json
[{
  "id": 1,
  "rule": [
    {
      "TRT_INFO": {
        "$$$$": "${CUSTOM1} > ${CUSTOM1}",
        "_?_:TRT_KEY": "_regex('1234-.*')",
        "YYK_ID": "_longerThan(3)",
        "PAT_ID": "_digit",
        "INSURE_TYPE": "_oneOf(건강보험,요향보험)",
        "INSURE_TYPE_CD": "_any",
        "DEPT_CD": "_any",
        "DEPT_NM": "_any",
        "DR_NM": "_longerThan(2) && _shorterThan(7)",
        "RCPT_GB": "_oneOf(입원,외래,응급,퇴원,중간)",
        "TRT_YMD": "_digitOr(-)",
        "TRT_S_YMD": "_date(${yyyy}-${mm}-${dd}) || _digitOr(-)",
        "TRT_E_YMD": "_any || _between(0, 1000)",
        "DR_CD": "_any",
        "TRT_AMT": "_any",
        "CUSTOM1": "_any",
        "CUSTOM2": "_any",
        "CUSTOM3": "_any",
        "CUSTOM4": "_any",
        "CUSTOM5": "_any"
      },
      "ISR_INFO": {
        "_?_:ISR_KEY": "_between(1, 100)",
        "HOSP_ID": "_any",
        "HOSP_NM": "_any",
        "PAT_ID": "_any",
        "PAT_NM": "_any",
        "PAT_HP": "_any",
        "PATIENT_TEL": "_any",
        "VIRTUAL_FAX": "_any",
        "INSURANT_NM": "_any",
        "INSURANT_JUMIN": "_any",
        "BENEFICIARY": "_any",
        "BENEFICIARY_JUMIN": "_any",
        "BANK_CD": "_any",
        "BANK_NAME": "_any",
        "BANK_NUMBER": "_any",
        "ACCIDENT_TYPE": "_any",
        "_?_:SOMEKEY": "_any",
        "$$$$": "${INSURER.amount} <= ${INSURANT_JUMIN}",
        "ACCIDENT_CONTENT": "_any",
        "INSURER": {
          "Code": "_any",
          "Name": "_any"
        }
      },
      "RCPT_HEADER": {
        "RCPT_ID": "_any",
        "RCPT_GB": "_any",
        "PAT_ID": "_any",
        "PAT_NM": "_any",
        "TRT_S_YMD": "_any",
        "TRT_E_YMD": "_any",
        "DEPT_CD": "_any",
        "DEPT_NM": "_any",
        "TRT_TYPE": "_any",
        "TRT_TYPE_CD": "_any",
        "TRT_AMT": "_any",
        "PAT_AMT": "_any",
        "PAID_AMT": "_any",
        "DISCNT_AMT": "_any",
        "DISCNT_DESC": "_any",
        "PAID_NOT_AMT": "_any",
        "PAID_NOT_AMT_DESC": "_any",
        "AMT_TO_PAY": "_any",
        "PAY_AMT": "_any",
        "OWN_AMT": "_any",
        "INSURER_AMT": "_any",
        "ALL_OWN_AMT": "_any",
        "SPC_AMT": "_any",
        "SPC_EX_AMT": "_any",
        "UP_LIMIT_AMT": "_any",
        "DRG_NO": "_any"
      },
      "RCPT_DETAIL": [
        {
          "RCPT_ID": "_any",
          "ITEM_NM": "_any",
          "ITEM_CD": "_any",
          "OWN_AMT": "_any",
          "INSURER_AMT": "_any",
          "ALL_OWN_AMT": "_any",
          "SPC_AMT": "_any",
          "SPC_EX_AMT": "_any"
        }
      ],
      "FEE_DETAIL": [
        {
          "RCPT_ID": "_any",
          "TRT_YMD": "_any",
          "TRT_CD": "_any",
          "TRT_NM": "_any",
          "SUGA_CD": "_any",
          "EDI_CD": "_any",
          "ITEM_NM": "_any",
          "UNIT_PRICE": "_any",
          "ITEM_CNT": "_any",
          "DAYS": "_any",
          "INS_TYPE": "_any",
          "CALC_AMT": "_any",
          "OWN_AMT": "_any",
          "INSURER_AMT": "_any",
          "ALL_OWN_AMT": "_any",
          "SPC_AMT": "_any",
          "SPC_EX_AMT": "_any"
        }
      ],
      "PRS_INFO": [
        {
          "RX_ID": "_digit && _longerThan(10)",
          "INSURE_TYPE_CD": "_any",
          "INSURE_TYPE_NM": "_any",
          "PAT_ID": "_any",
          "PAT_NM": "_any",
          "BIRTH_YMD": "_any",
          "PAT_SEX": "_any",
          "TRT_YMD": "_any",
          "HOSP_NM": "_any",
          "HOSP_CD": "_any",
          "HOSP_TEL": "_any",
          "HOSP_FAX": "_any",
          "HOSP_EMAIL": "_any",
          "DIAG_CD": "_any",
          "DR_NM": "_any",
          "DR_NO": "_any",
          "EDI_CD": "_any",
          "DRUG_NM": "_any",
          "DOSE_AMT_ONCE": "_any",
          "DOSE_CNT_PER_DAY": "_any",
          "DAYS_OF_DOSE": "_any",
          "METHOD": "_any"
        }
      ]
    }
  ]
}]
```

* transform (illustrative)
-----
node-path :: `/dataTune/transform`

```json
[
  {
    "id": 1,
    "rule": {
      "[$$]": "$[?(.TRT_INFO && .ISR_INFO && .RCPT_HEADER && .FEE_DETAIL && .PRS_INFO)]",
      "[$>]": {
        "{::}": "$.TRT_INFO",
        "{=>}": {
          "addItem4": "@.CUSTOM4",
          "subInsuranceCd": null,
          "addItem5": "@.CUSTOM5",
          "certificateDocId": null,
          "treatCls": "O",
          "writeDt": null,
          "patientTotalAmt": 8000,
          "endDt": "@.TRT_E_YMD",
          "issueUse": null,
          "deptNm": "@.DEPT_NM",
          "certificateCd": null,
          "insuranceClaimCd": "11100397-O-20220826-201110858-2022082601451-0102",
          "doctorId": "100036",
          "insuranceCd": "@.INSURE_TYPE_CD",
          "dgnNm": null,
          "billNo": "$.RCPT_HEADER.RCPT_ID",
          "certificateNm": null,
          "visitDt": "@.TRT_YMD",
          "doctorLicense": "80074",
          "deptCd": "@.DEPT_CD",
          "doctorNm": "@.DR_NM",
          "totalAmt": "@.TRT_AMT",
          "insuranceNm": "@.INSURE_TYPE",
          "visitTm": "1550",
          "receiptNo": 80,
          "receiptCls": null,
          "startDt": "@.TRT_S_YMD",
          "dgnCd": null,
          "addItem1": "@.CUSTOM1",
          "addItem2": "@.CUSTOM2",
          "addItem3": "@.CUSTOM3"
        },
        "receipt": {
          "{::}": "$.RCPT_HEADER",
          "{=>}": {
            "approvalNo": null,
            "reservationAmt": null,
            "patientId": "@.PAT_ID",
            "installmentPeriod": null,
            "endDt": "@.TRT_E_YMD",
            "deptNm": "@.DEPT_NM",
            "receiptConfirm": null,
            "cardValidity": null,
            "cardNo": null,
            "diseaseGrpNo": "@.DRG_NO",
            "startTm": "161153",
            "preAmtCash": 0,
            "etcAmtNm": null,
            "wellnessAmt": 0,
            "outpatientAmt": 0,
            "paymentTargetAmt": null,
            "cashTyNm": null,
            "futureAmt": 0,
            "billNo": "@.RCPT_ID",
            "cashApprovalNo": null,
            "patientAmt": "@.OWN_AMT",
            "patientNm": "@.PAT_NM",
            "roomNm": null,
            "realAmt": "@.AMT_TO_PAY",
            "cardKindNm": null,
            "selectMedicalYn": "N",
            "preAmtCheck": 8000,
            "preAmt": "@.PAID_AMT",
            "selectMedicalAmt": "@.SPC_AMT",
            "totalAmt": "@.TRT_AMT",
            "insureKindNm": "@.TRT_TYPE",
            "receiptNo": 80,
            "surtax": 0,
            "receiptNm": "900303",
            "exceptSelectMedicalAmt": "@.SPC_EX_AMT",
            "subType": "00",
            "preAmtCard": 8000,
            "addItem4": null,
            "addItem5": null,
            "discountAmt": "@.DISCNT_AMT",
            "receivableAmt": "@.PAID_NOT_AMT",
            "treatCls": "@.RCPT_GB",
            "nightHolidayYn": null,
            "patientTotalAmt": "@.PAT_AMT",
            "addItemAmt1": 0,
            "receiptTyNm": "01",
            "cashBillCardNo": null,
            "remark": null,
            "addItemAmt2": 0,
            "addItemAmt3": null,
            "issueNo": null,
            "preAmtCashBillcard": null,
            "insuranceClaimCd": "11100397-O-20220826-201110858-2022082601451-0102",
            "fullPatientAmt": "@.ALL_OWN_AMT",
            "rareSupportfund": null,
            "receiptAmt": "@.PAY_AMT",
            "issueDtTm": "202208261618",
            "cardPayment": null,
            "franchiseeNo": null,
            "cardNo2": null,
            "bloodAmt": 0,
            "cardNo3": null,
            "deptCd": "0102",
            "doctorNm": "김배근",
            "addItemAmt4": null,
            "insureAmt": "@.INSURER_AMT",
            "addItemAmt5": null,
            "endTm": "161304",
            "startDt": "@.TRT_S_YMD",
            "hospitalCls": "11",
            "customerNm": null,
            "upperLmtExcdAmt": "@.UP_LIMIT_AMT",
            "addItem1": null,
            "etcAmt": 0,
            "addItem2": "N",
            "receiptTm": "20220826년 08월 26일 16:18",
            "addItem3": null
          }
        },
        "receiptItem": {
          "[::]": "$.RCPT_DETAIL[0:]",
          "[=>]": {
            "patientAmt": "@.OWN_AMT",
            "addItem4": null,
            "addItem5": null,
            "accumulatedLclsCd": "@.ITEM_CD",
            "patientTotalAmt": 8065,
            "insuranceClaimCd": "11100397-O-20220826-201110858-2022082601451-0102",
            "fullPatientAmt": "@.ALL_OWN_AMT",
            "selectMedicalAmt": "@.SPC_AMT",
            "totalAmt": 16130,
            "insureAmt": "@.INSURER_AMT",
            "exceptSelectMedicalAmt": "@.SPC_EX_AMT",
            "accumulatedLclsNm": "@.ITEM_NM",
            "billNo": "2022082601451",
            "addItem1": null,
            "addItem2": null,
            "addItem3": null
          }
        },
        "receiptDetail": {
          "[::]": "$.FEE_DETAIL[0:]",
          "[=>]": {
            "enforceEndTm": null,
            "addItem4": null,
            "addItem5": null,
            "accumulatedLclsCd": "@.TRT_CD",
            "enforceDt": "@.TRT_YMD",
            "patientTotalAmt": 6855,
            "deptNm": null,
            "insuranceClaimCd": "11100397-O-20220826-201110858-2022082601451-0102",
            "fullPatientAmt": "@.ALL_OWN_AMT",
            "insuranceTargetCd": "@.INS_TYPE",
            "ediCd": "@.EDI_CD",
            "orderCntPer1Day": "@.ITEM_CNT",
            "orderQty": null,
            "accumulatedLclsNm": "@.TRT_NM",
            "medicalChrg": "@.SUGA_CD",
            "orderUnit": null,
            "enforceTm": null,
            "patientAmt": "@.OWN_AMT",
            "orderNm": "@.ITEM_NM",
            "orderDays": "@.DAYS",
            "doctorLicense": null,
            "itemCd": "@.TRT_CD",
            "doctorNm": null,
            "selectMedicalAmt": "@.SPC_AMT",
            "orderDt": null,
            "totalAmt": "@.CALC_AMT",
            "insureAmt": "@.INSURER_AMT",
            "unitCost": "@.UNIT_PRICE",
            "exceptSelectMedicalAmt": "@.SPC_EX_AMT",
            "enforceEndDt": null,
            "addItem1": null,
            "totalNonPayment": 0,
            "addItem2": null,
            "orderQtyPer1Time": null,
            "addItem3": null
          }
        },
        "prescriptionList": {
          "[::]": "$.PRS_INFO[0:]",
          "[=>]": {
            "specialCd": " ",
            "specialty": "순환기내과",
            "hospitalAbbr": null,
            "topMemo3": null,
            "topMemo2": null,
            "patientId": "@.PAT_ID",
            "topMemo1": null,
            "insureDetailCd": null,
            "benefitCtgCd": null,
            "doctorLicenseCls": "1",
            "deptNm": "순환기내과",
            "prescriptionAllNo": null,
            "insuranceCd": "@.INSURE_TYPE_CD",
            "orderDetailCont": "없음",
            "insureRelCls": "1",
            "diagnosisCd3": null,
            "diagnosisCd2": null,
            "patientNm": "@.PAT_NM",
            "insureDetailNm": "없음",
            "industrialAccHospCd": null,
            "prescriptionTm": null,
            "insureCardNo": "80523060191",
            "doctorLicense": "@.DR_NO",
            "nextVisitDt": null,
            "prescriptionPeriod": "5",
            "insureNm": "이화",
            "industrialAccDt": null,
            "doctorEmail": null,
            "orgUnitNo": null,
            "powderYn": "N",
            "receiptNo": "00080-2022082600519_80",
            "industrialAccCompNm": null,
            "orgUnitNm": null,
            "patriotNo": null,
            "hospitalNm": "@.HOSP_NM",
            "hospitalNo": "$.TRT_INFO.YYK_ID",
            "addItem4": null,
            "centerMemo3": "G",
            "addItem5": null,
            "hospitalEmail": "@.HOSP_EMAIL",
            "addItem6": 80,
            "hospitalAddr": "서울특별시 영등포구 여의대방로53길 22(신길동)  ",
            "gender": "@.PAT_SEX",
            "centerNm": null,
            "hospitalHpUrl": "www.suangae.co.kr",
            "centerMemo2": "01049234270",
            "centerMemo1": "",
            "doctorLicenseClsNm": "의사",
            "doctorTel": null,
            "prescriptionIntNo": null,
            "prescriptionDt": "@.TRT_YMD",
            "selfPaymentCd": null,
            "bottomMemo3": null,
            "diagnosisCd1": "@.DIAG_CD",
            "dispenseMemo": null,
            "bottomMemo2": null,
            "address": "서울특별시 영등포구 여의대방로43나길 25, 105동 2105호 (신길동, 삼환아파트)",
            "bottomMemo1": null,
            "industrialAccPatientNo": null,
            "hospitalFax": "@.HOSP_FAX",
            "hospitalTel": "$.PRS_INFO[0].HOSP_TEL",
            "prescriptionNo": "@.RX_ID",
            "deptCd": "02",
            "specialMemo": null,
            "doctorNm": "@.DR_NM",
            "insuranceNm": "@.INSURE_TYPE_NM",
            "identificationNo": "@.BIRTH_YMD",
            "addItem1": null,
            "age": "57",
            "addItem2": null,
            "addItem3": "N",
            "prcpMixNo": null,
            "drugPackageCls": "0",
            "drugMemo": null,
            "drugAddItem1": null,
            "drugAddItem2": null,
            "drugAddItem3": null,
            "kdCode": "@.EDI_CD",
            "drugMethCd": "DPC",
            "drugDetail": "@.METHOD",
            "medicalChrg": "RSVST5",
            "drugIngredientNm": "454003ATB",
            "drugNm": "@.DRUG_NM",
            "doseDay": "@.DAYS_OF_DOSE",
            "selfPaymentRateCd": null,
            "drugNmEng": "650700540",
            "orderKind": "0",
            "doseRouteCls": "0",
            "drugInOutCls": "2",
            "drugAddItem4": null,
            "drugAddItem5": null,
            "doseTotalQty": 1,
            "psychCd": "1",
            "prnCd": "0",
            "drugNmCd": "0",
            "drugInsureCls": "1",
            "doseQtyPer1Tim": "@.DOSE_AMT_ONCE",
            "doseQtyPer1Day": "@.DOSE_CNT_PER_DAY",
            "doseUnit": "정"
          }
        }
      }
    }
  }
]
```

#### 4. create control node in zookeeper 

path :: `/dataTune/_control`

This is for control DataTune at runtime.

create with zoo-navigater
1. connect `localhost:9000` with web-browser)

#### 5. create kafka topic to read

topic-name :: `In`

with kafka-UI 
1. connect `localhost:8080` with web-browser)
2. Add a topic

#### 5. execute with options
```
   -kf:url localhost:9094
   -kf:in In
   -kf:out Out
   -kf:err Err
   -zk:url localhost:2181
   -zk:classify /dataTune/classify
   -zk:validate /dataTune/validate
   -zk:transform /dataTune/transform
   -zk:ctrl /dataTune/_control
   -es:url localhost:9200
```

#### 6. examine

insert some json to `In` topic
( nothing will happen until then.)

see what happen
1. kafka ( `localhost:8080`) :: see, Out-topic, Err-topic (this will be automatically created.)
2. elasticSearch ( `localhost:8080`) :: ses Some indices( indices will be automatically created.)

sample Json
```json
[
  {
    "TRT_INFO": {
      "TRT_KEY": "1234-111aa-dasdsad",
      "YYK_ID": "41359160",
      "PAT_ID": "1",
      "RCPT_GB": "외래",
      "TRT_YMD": "2022-07-18",
      "TRT_S_YMD": "2022-07-18",
      "TRT_E_YMD": "2022-07-18",
      "INSURE_TYPE": "건강보험",
      "INSURE_TYPE_CD": "P01",
      "DEPT_CD": "D01",
      "DEPT_NM": "내과",
      "DR_NM": "김x희",
      "DR_CD": "6",
      "TRT_AMT": "5000",
      "CUSTOM1": "20220718",
      "CUSTOM2": "20220718",
      "CUSTOM3": "20220718",
      "CUSTOM4": "20220718",
      "CUSTOM5": "20220718"
    },
    "ISR_INFO": {
      "ISR_KEY": "aaaaa-bbbbb-cccc...",
      "HOSP_ID": "41359160",
      "HOSP_NM": "365속시원한가정의학과의원",
      "PAT_ID": "1",
      "PAT_NM": "홍길동",
      "PAT_HP": "01000000000",
      "PATIENT_TEL": "4444",
      "VIRTUAL_FAX": "5555",
      "INSURANT_NM": "6666",
      "INSURANT_JUMIN": "1111112222222",
      "BENEFICIARY": "aaaaaaaa",
      "BENEFICIARY_JUMIN": "2222223333333",
      "BANK_CD": "012",
      "BANK_NAME": "KEB하나은행",
      "BANK_NUMBER": "1231412312",
      "ACCIDENT_TYPE": "질병",
      "ACCIDENT_CONTENT": "회의가 너무 많아 골이 깨짐",
      "INSURER": {
        "Code": "1.2.410.200071.1.1.1.1.2.14",
        "Name": "교보생명",
        "amount" : "1234"
      }
    },
    "RCPT_HEADER": {
      "RCPT_ID": "20240306-0002",
      "RCPT_GB": "외래",
      "PAT_ID": "1",
      "PAT_NM": "홍길동",
      "TRT_S_YMD": "2022-07-18",
      "TRT_E_YMD": "2022-07-18",
      "DEPT_CD": "D06",
      "DEPT_NM": "신경외과",
      "TRT_TYPE": "건강보험",
      "TRT_TYPE_CD": "P01",
      "TRT_AMT": "16970",
      "PAT_AMT": "5000",
      "PAID_AMT": "0",
      "DISCNT_AMT": "0",
      "DISCNT_DESC": "==",
      "PAID_NOT_AMT": "0",
      "PAID_NOT_AMT_DESC": "==",
      "AMT_TO_PAY": "5000",
      "PAY_AMT": "5000",
      "OWN_AMT": "5000",
      "INSURER_AMT": "11970",
      "ALL_OWN_AMT": "0",
      "SPC_AMT": "0",
      "SPC_EX_AMT": "0",
      "UP_LIMIT_AMT": "0",
      "DRG_NO": "=="
    },
    "RCPT_DETAIL": [
      {
        "RCPT_ID": "20240306-0002",
        "ITEM_NM": "진찰료",
        "ITEM_CD": "C01",
        "OWN_AMT": "5091",
        "INSURER_AMT": "11879",
        "ALL_OWN_AMT": "0",
        "SPC_AMT": "0",
        "SPC_EX_AMT": "0"
      }
    ],
    "FEE_DETAIL": [
      {
        "RCPT_ID": "20220718_1",
        "TRT_YMD": "2022-07-18",
        "TRT_CD": "C05",
        "TRT_NM": "투약조제(약품료)",
        "SUGA_CD": "7901",
        "EDI_CD": "641602060",
        "ITEM_NM": "(원외)우루사정200mg 대웅제약",
        "UNIT_PRICE": "0",
        "ITEM_CNT": "3.0000",
        "DAYS": "1",
        "INS_TYPE": "급여",
        "CALC_AMT": "0",
        "OWN_AMT": "0",
        "INSURER_AMT": "0",
        "ALL_OWN_AMT": "0",
        "SPC_AMT": "0",
        "SPC_EX_AMT": "0"
      },
      {
        "RCPT_ID": "20220718_1",
        "TRT_YMD": "2022-07-18",
        "TRT_CD": "C01",
        "TRT_NM": "진찰료",
        "SUGA_CD": "==",
        "EDI_CD": "AA154",
        "ITEM_NM": "초진진찰료",
        "UNIT_PRICE": "16970",
        "ITEM_CNT": "1.0000",
        "DAYS": "1",
        "INS_TYPE": "급여",
        "CALC_AMT": "16970",
        "OWN_AMT": "5091",
        "INSURER_AMT": "11879",
        "ALL_OWN_AMT": "0",
        "SPC_AMT": "0",
        "SPC_EX_AMT": "0"
      }
    ],
    "PRS_INFO": [
      {
        "RX_ID": "2022071800001",
        "INSURE_TYPE_CD": "P01",
        "INSURE_TYPE_NM": "건강보험",
        "PAT_ID": "1",
        "PAT_NM": "홍길동",
        "BIRTH_YMD": "1985-06-14",
        "PAT_SEX": "남성",
        "TRT_YMD": "2022-07-18",
        "HOSP_NM": "테스트의원",
        "HOSP_CD": "16001478",
        "HOSP_TEL": "02-310-9160",
        "HOSP_FAX": "==",
        "HOSP_EMAIL": "==",
        "DIAG_CD": "J00",
        "DR_NM": "김병희",
        "DR_NO": "106001",
        "EDI_CD": "641602060",
        "DRUG_NM": "(641602060)우루사정200mg 대웅제약",
        "DOSE_AMT_ONCE": "1.0000",
        "DOSE_CNT_PER_DAY": "3",
        "DAYS_OF_DOSE": "1",
        "METHOD": "=="
      }
    ]
  }
]

```

control with zookeeper control-node

1. change some rule in zookeeper node
2. write `update` in control-node (`/dataTune/_control`)
