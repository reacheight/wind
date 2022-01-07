import React from 'react';

import Analysis from './Analysis/Analysis'
import Header from './Header/Header';
import Form from 'react-bootstrap/Form';
import Spinner from 'react-bootstrap/Spinner';

import styles from './App.module.css'

class App extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      matchId: '',
      analysis: {},
      loading: false,
      error: false
    };

    this.handleChange = this.handleChange.bind(this)
    this.handleSubmit = this.handleSubmit.bind(this)
  }

  handleChange(event) {
    this.setState({ matchId: event.target.value })
  }

  handleSubmit(event) {
    this.setState({ analysis: {}, loading: true, error: false })
    fetch(`http://localhost:8080/analysis/${this.state.matchId}`)
      .then(response => {
        if (!response.ok) {
          this.setState({ error: true })
        }
        return response.json()
      })
      .then(json => {
        this.setState({ loading: false, analysis: json })
      })
      .catch(e => {
        this.setState({ loading: false, error: true })
      })
    event.preventDefault()
  }

  render() {
    return (
      <div className={styles.app}>
        <Header />
        <form className={styles.input} onSubmit={this.handleSubmit}>
          <Form.Control type="text" placeholder="Enter match id" value={this.state.matchId} onChange={this.handleChange} />
        </form>
        {this.state.loading &&
          <div className={styles.spinner}>
            <Spinner variant="light" animation="border" role="status"></Spinner>
          </div>
        }
        <Analysis analysis={this.state.analysis} />
        {this.state.error &&
          <div className={styles.error}> Error occured :( </div>
        }
      </div>
    );
  }
}

export default App;
