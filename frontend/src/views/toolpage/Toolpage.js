import React from 'react'
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CCardImage,
  CCardText,
  CCardTitle,
  CCol,
  CPlaceholder,
  CRow,
} from '@coreui/react'
import { DocsExample } from 'src/components'

import MySQLImg from 'src/assets/images/MySQL.jpg'
import MsSQLImg from 'src/assets/images/MsSQL.png'
import PostgreSQLImg from 'src/assets/images/postgresql.jpeg'
import JSONImg from 'src/assets/images/Json.png'





const Toolpage = () => {
    
    return (
        
        <CRow>
            
            <CCard style={{ width: '18rem' }}>
            <CCardImage orientation="top" src={MySQLImg} />
            <CCardBody>
                <CCardTitle>NLP to MySQL Query</CCardTitle>
                <CCardText>
                    Quickly generate SQL command to MySQL database and retrieve any information you desired.
                </CCardText>
                <CButton color="primary" href="http://localhost:3000/#/NLP2MySQL">Use</CButton>
            </CCardBody>
            </CCard>

            <CCard style={{ width: '18rem' }}>
            <CCardImage orientation="top" src={PostgreSQLImg} />
            <CCardBody>
                <CCardTitle>NLP to PostgreSQL Query</CCardTitle>
                <CCardText>
                    Quickly generate SQL command to PostgreSQL database and retrieve any information you desired.
                </CCardText>
                <CButton color="primary" href="http://localhost:3000/#/NLP2PostGreSQL">Use</CButton>
            </CCardBody>
            </CCard>

            <CCard style={{ width: '18rem' }}>
            <CCardImage orientation="top" src={MsSQLImg} />
            <CCardBody>
                <CCardTitle>Microsoft SQL Server to Query</CCardTitle>
                <CCardText>
                    Quickly generate SQL command to Microsoft SQL Server database and retrieve any information you desired.
                </CCardText>
                <CButton color="primary" href="http://localhost:3000/#/NLP2MSSQL">Use</CButton>
            </CCardBody>
            </CCard>

            <CCard style={{ width: '18rem' }}>
            <CCardImage orientation="top" src={JSONImg} />
            <CCardBody>
                <CCardTitle>NLP to JSON-SCHEMA Query</CCardTitle>
                <CCardText>
                    Quickly generate SQL based on JSON-SCHEMA.
                </CCardText>
                <CButton color="primary" href="http://localhost:3000/#/NLP2JSONSchema">Use</CButton>
            </CCardBody>
            </CCard>



        </CRow>
        
        
    )
}

export default Toolpage