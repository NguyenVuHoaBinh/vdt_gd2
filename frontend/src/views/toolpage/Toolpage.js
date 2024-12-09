import React, { useEffect } from 'react'
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
import annyang from 'annyang'

import MySQLImg from 'src/assets/images/MySQL.jpg'
import MsSQLImg from 'src/assets/images/MsSQL.png'
import PostgreSQLImg from 'src/assets/images/postgresql.jpeg'
import JSONImg from 'src/assets/images/Json.png'
import VPOSImg from 'src/assets/images/vPOS.png'
import mydioImg from 'src/assets/images/Mydio.png'

const Toolpage = () => {
  useEffect(() => {
    if (annyang) {
      // Define commands
      const commands = {
        'open MySQL tool': () => {
          window.location.href = 'http://localhost:3000/#/NLP2MySQL'
        },
        'open PostgreSQL tool': () => {
          window.location.href = 'http://localhost:3000/#/NLP2PostGreSQL'
        },
        'open Microsoft SQL tool': () => {
          window.location.href = 'http://localhost:3000/#/NLP2MSSQL'
        },
        'open JSON tool': () => {
          window.location.href = 'http://localhost:3000/#/NLP2JSONSchema'
        },
        'open vPOS tool': () => {
          window.location.href = 'http://localhost:3000/#/NLP2VPOS'
        },
        'open Mydio tool': () => {
          window.location.href = 'https://cd83-58-187-4-197.ngrok-free.app/#/NLP2MYDIO'
        },
        'hi vian': () => {
          window.location.href = 'https://cd83-58-187-4-197.ngrok-free.app/#/NLP2MYDIO'
        },
      }

      // Add our commands to annyang
      annyang.addCommands(commands)

      // Start listening
      annyang.start()

    }
  }, [])

  return (
    <CRow>
      <CCard style={{ width: '18rem' }}>
        <CCardImage orientation="top" src={MySQLImg} />
        <CCardBody>
          <CCardTitle>NLP to MySQL Query</CCardTitle>
          <CCardText>
            Quickly generate SQL command to MySQL database and retrieve any information you desired.
          </CCardText>
          <CButton color="primary" href="http://localhost:3000/#/NLP2MySQL">
            Use
          </CButton>
        </CCardBody>
      </CCard>

      <CCard style={{ width: '18rem' }}>
        <CCardImage orientation="top" src={PostgreSQLImg} />
        <CCardBody>
          <CCardTitle>NLP to PostgreSQL Query</CCardTitle>
          <CCardText>
            Quickly generate SQL command to PostgreSQL database and retrieve any information you desired.
          </CCardText>
          <CButton color="primary" href="http://localhost:3000/#/NLP2PostGreSQL">
            Use
          </CButton>
        </CCardBody>
      </CCard>

      <CCard style={{ width: '18rem' }}>
        <CCardImage orientation="top" src={MsSQLImg} />
        <CCardBody>
          <CCardTitle>Microsoft SQL Server to Query</CCardTitle>
          <CCardText>
            Quickly generate SQL command to Microsoft SQL Server database and retrieve any information you desired.
          </CCardText>
          <CButton color="primary" href="http://localhost:3000/#/NLP2MSSQL">
            Use
          </CButton>
        </CCardBody>
      </CCard>

      <CCard style={{ width: '18rem' }}>
        <CCardImage orientation="top" src={JSONImg} />
        <CCardBody>
          <CCardTitle>NLP to JSON-SCHEMA Query</CCardTitle>
          <CCardText>Quickly generate SQL based on JSON-SCHEMA.</CCardText>
          <CButton color="primary" href="http://localhost:3000/#/NLP2JSONSchema">
            Use
          </CButton>
        </CCardBody>
      </CCard>

      <CCard style={{ width: '18rem' }}>
        <CCardImage orientation="top" src={VPOSImg} />
        <CCardBody>
          <CCardTitle>NLP to vPOS</CCardTitle>
          <CCardText>ASR SELLING</CCardText>
          <CButton color="primary" href="http://localhost:3000/#/NLP2VPOS">
            Use
          </CButton>
        </CCardBody>
      </CCard>

      <CCard style={{ width: '18rem' }}>
        <CCardImage orientation="top" src={mydioImg} />
        <CCardBody>
          <CCardTitle>Mydio</CCardTitle>
          <CCardText>Speaking Books</CCardText>
          <CButton color="primary" href="https://cd83-58-187-4-197.ngrok-free.app/#/NLP2MYDIO">
            Use
          </CButton>
        </CCardBody>
      </CCard>
    </CRow>
  )
}

export default Toolpage
