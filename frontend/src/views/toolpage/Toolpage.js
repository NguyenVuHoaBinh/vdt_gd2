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
                <CButton color="primary" href="http://localhost:3000/#/mysqltool">Use</CButton>
            </CCardBody>
            </CCard>

            <CCard style={{ width: '18rem' }}>
            <CCardImage orientation="top" src={MsSQLImg} />
            <CCardBody>
                <CCardTitle>Microsoft SQL Server to Query</CCardTitle>
                <CCardText>
                    Quickly generate SQL command to Microsoft SQL Server database and retrieve any information you desired.
                </CCardText>
                <CButton color="primary" href="#">Use</CButton>
            </CCardBody>
            </CCard>
        </CRow>
        
        
    )
}

export default Toolpage