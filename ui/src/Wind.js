import React, { useState } from 'react';

import Analysis from './Analysis/Analysis'
import Header from './Header/Header';
import Form from 'react-bootstrap/Form';
import Spinner from 'react-bootstrap/Spinner';

import styles from './App.module.css'

const Wind = () => {
  const API_ENDPOINT = process.env.REACT_APP_API_ENDPOINT;

  const [matchId, setMatchId] = useState('')
  const [analysis, setAnalysis] = useState({})
  const [loading, setLoading] = useState(false)
  const [isError, setIsError] = useState(false)

  const getAnalysis = event => {
    setAnalysis({})
    setLoading(true)
    setIsError(false)

    fetch(`${API_ENDPOINT}/analysis/${matchId}`)
      .then(response => {
        if (!response.ok) {
          setIsError(true)
        }
        return response.json()
      })
      .then(json => {
        setLoading(false)
        setAnalysis(json)
      })
      .catch(e => {
        setLoading(false)
        setIsError(true)
      })
      
    event.preventDefault()
  }

  return (
    <div className={styles.app}>
      <Header />
      <form className={styles.input} onSubmit={getAnalysis}>
        <Form.Control type='text' placeholder='Enter match id' value={matchId} onChange={event => setMatchId(event.target.value)} />
      </form>
      {loading &&
        <div className={styles.spinner}>
          <Spinner variant='light' animation='border' role='status'></Spinner>
        </div>
      }
      <Analysis analysis={analysis} />
      {isError &&
        <div className={styles.error}> Error occured :( </div>
      }
    </div>
  )
}

export default Wind
